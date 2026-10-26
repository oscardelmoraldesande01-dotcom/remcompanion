package com.remmod.remcompanion.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;

/**
 * Rem: NPC compañera. Se hace amiga de quien le regala comida/flores,
 * habla con distintos diálogos según el nivel de afecto, y lucha a tu lado.
 */
public class RemEntity extends TamableAnimal {
    public static final int MAX_AFFECTION = 1000;
    private static final int LINES_PER_STAGE = 3;
    private static final long TALK_COOLDOWN = 2400L;  // 2 min
    private static final long GIFT_COOLDOWN = 1200L;  // 1 min

    private int affection = 0;
    private long lastTalk = -100000L;
    private long lastGift = -100000L;

    public RemEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.updateStats();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D);
    }

    // ------------------------------------------------------------ IA
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1D, 8.0F, 2.0F, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                e -> this.isTame() && !(e instanceof Creeper)));
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof RemEntity) return false;
        if (target instanceof Player p && owner instanceof Player o && !o.canHarmPlayer(p)) return false;
        return super.wantsToAttack(target, owner);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Si te quiere lo bastante, te cura cuando estás mal herido
        if (!level().isClientSide && isTame() && affection >= 300 && tickCount % 100 == 0) {
            LivingEntity owner = getOwner();
            if (owner != null && owner.distanceToSqr(this) < 256.0D
                    && owner.getHealth() < owner.getMaxHealth() * 0.5F) {
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                ((ServerLevel) level()).sendParticles(ParticleTypes.HEART,
                        owner.getX(), owner.getY() + 1.5D, owner.getZ(), 4, 0.3, 0.3, 0.3, 0.02);
            }
        }
    }

    // ------------------------------------------------------------ Interacción
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        long now = level().getGameTime();

        // Si ya es compañera de otro jugador
        if (isTame() && !isOwnedBy(player)) {
            say(player, "dialogue.remcompanion.rem.other_owner");
            return InteractionResult.CONSUME;
        }

        // Regalo
        if (!stack.isEmpty() && isGift(stack)) {
            if (now - lastGift < GIFT_COOLDOWN) {
                say(player, "dialogue.remcompanion.rem.too_many_gifts");
                return InteractionResult.CONSUME;
            }
            lastGift = now;
            int gain = giftValue(stack);
            if (!player.getAbilities().instabuild) stack.shrink(1);

            boolean first = !isTame();
            if (first) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                this.setHealth(this.getMaxHealth());
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            addAffection(gain);
            hearts();
            this.playSound(SoundEvents.PLAYER_BURP, 0.6F, 1.4F);
            say(player, first ? "dialogue.remcompanion.rem.first_gift" : "dialogue.remcompanion.rem.gift");
            showAffection(player);
            return InteractionResult.CONSUME;
        }

        // Aún no es compañera: solo habla con desconfianza
        if (!isTame()) {
            sayStage(player, 0);
            return InteractionResult.CONSUME;
        }

        // Agacharse + clic: sentarse / seguir
        if (player.isShiftKeyDown()) {
            boolean sit = !isOrderedToSit();
            setOrderedToSit(sit);
            this.navigation.stop();
            this.setTarget(null);
            say(player, sit ? "dialogue.remcompanion.rem.stay" : "dialogue.remcompanion.rem.follow");
            return InteractionResult.CONSUME;
        }

        // Hablar
        sayStage(player, stage());
        if (now - lastTalk >= TALK_COOLDOWN) {
            lastTalk = now;
            addAffection(3);
            hearts();
        }
        showAffection(player);
        return InteractionResult.CONSUME;
    }

    private boolean isGift(ItemStack s) {
        return s.isEdible() || s.is(ItemTags.FLOWERS) || s.is(Items.CAKE);
    }

    private int giftValue(ItemStack s) {
        if (s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE) || s.is(Items.CAKE)
                || s.is(Items.PUMPKIN_PIE) || s.is(Items.COOKIE) || s.is(Items.GOLDEN_CARROT)) return 30;
        if (s.is(ItemTags.FLOWERS)) return 18;
        return 8;
    }

    // ------------------------------------------------------------ Relación
    public int stage() {
        if (affection < 150) return 1;
        if (affection < 400) return 2;
        if (affection < 700) return 3;
        return 4;
    }

    private void addAffection(int amount) {
        affection = Math.max(0, Math.min(MAX_AFFECTION, affection + amount));
        updateStats();
    }

    /** Cuanto más te quiere, más fuerte y resistente es. */
    private void updateStats() {
        var atk = getAttribute(Attributes.ATTACK_DAMAGE);
        var hp = getAttribute(Attributes.MAX_HEALTH);
        if (atk != null) atk.setBaseValue(6.0D + affection / 125.0D);          // 6 -> 14
        if (hp != null) hp.setBaseValue(40.0D + Math.min(20, affection / 50)); // 40 -> 60
    }

    private void hearts() {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.9D, getZ(), 6, 0.4, 0.3, 0.4, 0.02);
        }
    }

    private void say(Player player, String key) {
        MutableComponent msg = Component.literal("<").append(getName().copy().withStyle(ChatFormatting.AQUA))
                .append(Component.literal("> ")).append(Component.translatable(key));
        player.sendSystemMessage(msg);
    }

    private void sayStage(Player player, int stage) {
        String name = stage == 0 ? "stranger" : "s" + stage;
        say(player, "dialogue.remcompanion.rem." + name + "." + random.nextInt(LINES_PER_STAGE));
    }

    private void showAffection(Player player) {
        player.displayClientMessage(Component.literal("\u2764 " + affection + "/" + MAX_AFFECTION + "  ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.translatable("stage.remcompanion.rem." + stage()).withStyle(ChatFormatting.GRAY)), true);
    }

    // ------------------------------------------------------------ Varios
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide && getOwner() instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("dialogue.remcompanion.rem.death").withStyle(ChatFormatting.RED));
        }
    }

    @Nullable @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PLAYER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PLAYER_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Affection", affection);
        tag.putLong("LastTalk", lastTalk);
        tag.putLong("LastGift", lastGift);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        affection = tag.getInt("Affection");
        lastTalk = tag.getLong("LastTalk");
        lastGift = tag.getLong("LastGift");
        updateStats();
    }
}

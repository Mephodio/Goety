package com.Polarice3.Goety.common.blocks;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.blocks.entities.HauntedMirrorBlockEntity;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayerRotationPacket;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.BlockFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class HauntedMirrorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D,
            16.0D, 16.0D, 5.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 10.0D,
            16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D,
            5.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(10.0D, 0.0D, 0.0D,
            16.0D, 16.0D, 16.0D);

    public HauntedMirrorBlock() {
        super(Properties.of()
                .pushReaction(PushReaction.BLOCK)
                .mapColor(MapColor.GOLD)
                .strength(1.0F, 3600000.0F)
                .lightLevel((blockState) -> 6)
                .noOcclusion()
                .sound(SoundType.GLASS));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER).setValue(LIT, Boolean.FALSE));
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        pLevel.setBlock(pPos.above(), pState.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        /*BlockEntity tileentity = pLevel.getBlockEntity(pPos);
        if (pPlacer instanceof Player){
            if (tileentity instanceof MagicMirrorBlockEntity blockEntity){
                blockEntity.setOwner(pPlacer);
            }
        }*/
    }

    public void playerWillDestroy(Level p_52755_, BlockPos p_52756_, BlockState p_52757_, Player p_52758_) {
        if (!p_52755_.isClientSide && p_52758_.isCreative()) {
            BlockFinder.preventCreativeDropFromBottomPart(p_52755_, p_52756_, p_52757_, p_52758_);
        }

        super.playerWillDestroy(p_52755_, p_52756_, p_52757_, p_52758_);
    }

    public boolean canSurvive(BlockState blockState, LevelReader level, BlockPos blockPos) {
        BlockPos blockpos = blockPos.below();
        BlockState blockstate = level.getBlockState(blockpos);
        return blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? super.canSurvive(blockState, level, blockPos) : blockstate.is(this);
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        DoubleBlockHalf doubleblockhalf = pState.getValue(HALF);
        if (pFacing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.LOWER == (pFacing == Direction.UP)) {
            return pFacingState.is(this) && pFacingState.getValue(HALF) != doubleblockhalf ? pState.setValue(FACING, pFacingState.getValue(FACING)).setValue(LIT, pFacingState.getValue(LIT)) : Blocks.AIR.defaultBlockState();
        } else {
            return doubleblockhalf == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_51750_) {
        BlockState blockstate = this.defaultBlockState();
        LevelReader levelreader = p_51750_.getLevel();
        BlockPos blockpos = p_51750_.getClickedPos();

        for(Direction direction : p_51750_.getNearestLookingDirections()) {
            if (direction.getAxis() != Direction.Axis.Y) {
                blockstate = blockstate.setValue(FACING, direction).setValue(HALF, DoubleBlockHalf.LOWER);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate;
                }
            }
        }
        return null;
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    public VoxelShape getShape(BlockState p_58114_, BlockGetter p_58115_, BlockPos p_58116_, CollisionContext p_58117_) {
        return switch (p_58114_.getValue(FACING)) {
            case SOUTH -> SOUTH_AABB;
            default -> NORTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
        };
    }

    public BlockState rotate(BlockState p_58109_, Rotation p_58110_) {
        return p_58109_.setValue(FACING, p_58110_.rotate(p_58109_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_58106_, Mirror p_58107_) {
        return p_58106_.rotate(p_58107_.getRotation(p_58106_.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_58112_) {
        p_58112_.add(FACING, HALF, LIT);
    }

    public static Direction getDirection(BlockState blockState){
        return blockState.getValue(FACING);
    }

    public static boolean isBlockTopOfMirror(BlockState blockState) {
        return blockState.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if(!level.isClientSide && entity.tickCount % 5 == 1 && (entity instanceof LivingEntity || entity instanceof ItemEntity)) {
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();
            int yShift = 0;
            if(!isBlockTopOfMirror(blockState)) {
                ++y;
                if(entity.getBbHeight() <= 1.0F) {
                    yShift = -1;
                }

                if(level.getBlockState(new BlockPos(x, y, z)).getBlock() != this) {
                    return;
                }
            }

            BlockPos blockPos1 = new BlockPos(x, y + yShift, z);
            AABB box = this.getBlockBoundingBox(blockPos1, blockState);
            float validWidth = EntityType.PLAYER.getWidth();
            float validHeight = EntityType.PLAYER.getHeight();
            boolean validSize = entity.getBbWidth() <= validWidth && entity.getBbHeight() <= validHeight;
            if(entity.getBoundingBox().intersects(box) && validSize) {
                Direction direction = blockState.getValue(FACING);
                int dx = direction.getStepX();
                int dz = direction.getStepZ();
                float xShift = -direction.getStepX();
                float zShift = -direction.getStepZ();
                Direction requiredSide = direction.getOpposite();
                for(int i = 1; i < 32; ++i) {
                    int newX = x + (dx * i);
                    int newZ = z + (dz * i);
                    BlockPos blockPos2 = new BlockPos(newX, y, newZ);
                    BlockState blockState1 = level.getBlockState(blockPos2);

                    if(blockState1.getBlock() == this) {
                        Direction direction1 = blockState1.getValue(FACING);
                        if (direction1 == requiredSide) {
                            entity.teleportTo(0.5D + (double) newX - (double) xShift, (double) (y - 1) + 0.01D, 0.5D + (double) newZ - (double) zShift);
                            this.teleportHit(level, entity);
                            return;
                        }
                    }
                }
                for (int i = 2; i < 16; ++i) {
                    int newY = y + i;
                    BlockPos blockPos2 = new BlockPos(x, newY, z);
                    BlockState blockState1 = level.getBlockState(blockPos2);
                    if(blockState1.getBlock() == this) {
                        BlockState blockState2 = level.getBlockState(blockPos2);
                        if(getDirection(blockState2) == direction) {
                            if(isBlockTopOfMirror(blockState2)) {
                                --newY;
                            }

                            entity.teleportTo(0.5D + (double)x + (double)xShift, (double)newY + 0.01D, 0.5D + (double)z + (double)zShift);
                            this.teleportHit(level, entity);
                            float turnAround = entity.rotate(Rotation.CLOCKWISE_180);
                            entity.setYRot(turnAround);
                            entity.yRotO = turnAround;
                            if (entity instanceof ServerPlayer serverPlayer){
                                ModNetwork.sendTo(serverPlayer, new SPlayerRotationPacket(turnAround, serverPlayer.getXRot()));
                            }
                            return;
                        }
                    }
                }
                for (int i = 2; i < 16; ++i) {
                    int newY = y - i;
                    BlockPos blockPos2 = new BlockPos(x, newY, z);
                    BlockState blockState1 = level.getBlockState(blockPos2);
                    if(blockState1.getBlock() == this) {
                        BlockState blockState2 = level.getBlockState(blockPos2);
                        if(getDirection(blockState2) == direction) {
                            if(isBlockTopOfMirror(blockState2)) {
                                --newY;
                            }

                            entity.teleportTo(0.5D + (double)x + (double)xShift, (double)newY + 0.01D, 0.5D + (double)z + (double)zShift);
                            this.teleportHit(level, entity);
                            float turnAround = entity.rotate(Rotation.CLOCKWISE_180);
                            entity.setYRot(turnAround);
                            entity.yRotO = turnAround;
                            if (entity instanceof ServerPlayer serverPlayer){
                                ModNetwork.sendTo(serverPlayer, new SPlayerRotationPacket(turnAround, serverPlayer.getXRot()));
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    public AABB getBlockBoundingBox(BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        float minX = 0.0F;
        float minY = 0.0F;
        float minZ = 0.0F;
        float maxX = 1.0F;
        float maxY = 1.0F;
        float maxZ = 1.0F;
        if(direction == Direction.SOUTH) {
            minZ = 0.32F;
        } else if(direction == Direction.NORTH) {
            maxZ = 0.68F;
        } else if(direction == Direction.WEST) {
            minX = 0.32F;
        } else if(direction == Direction.EAST) {
            maxX = 0.68F;
        }

        return new AABB(blockPos.getX() + minX, blockPos.getY() + minY, blockPos.getZ() + minZ, blockPos.getX() + maxX, blockPos.getY() + maxY, blockPos.getZ() + maxZ);
    }

    public void teleportHit(Level level, Entity entity){
        if (level instanceof ServerLevel serverLevel){
            for (int i = 0; i < serverLevel.random.nextInt(10) + 10; ++i) {
                serverLevel.sendParticles(ModParticleTypes.SUMMON.get(), entity.getRandomX(1.5D), entity.getRandomY(), entity.getRandomZ(1.5D), 0, 0.0F, 0.0F, 0.0F, 1.0F);
            }
        }
        level.gameEvent(GameEvent.TELEPORT, entity.position(), GameEvent.Context.of(entity));
        if (!entity.isSilent()) {
            level.playSound(null, entity.xo, entity.yo, entity.zo, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 0.5F, 0.75F);
            level.playSound(null, entity.xo, entity.yo, entity.zo, ModSounds.CAST_SPELL.get(), entity.getSoundSource(), 1.0F, 0.75F);
            entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.5F, 0.75F);
            entity.playSound(ModSounds.CAST_SPELL.get(), 1.0F, 0.75F);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new HauntedMirrorBlockEntity(p_153215_, p_153216_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_222100_, BlockState p_222101_, BlockEntityType<T> p_222102_) {
        return (world, pos, state, blockEntity) -> {
            if (!world.isClientSide) {
                if (blockEntity instanceof HauntedMirrorBlockEntity blockEntity1) {
                    blockEntity1.tick();
                }
            }
        };
    }
}

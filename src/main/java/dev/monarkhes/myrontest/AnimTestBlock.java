package dev.monarkhes.myrontest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;

import java.util.Random;

public class AnimTestBlock extends Block {
    public static final IntProperty FRAME = IntProperty.of("frame", 0, 249);

    public AnimTestBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FRAME, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FRAME);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, state.with(FRAME, (state.get(FRAME) + 1) % 250));

        TickScheduler<Block> tickScheduler = world.getBlockTickScheduler();

        if (!tickScheduler.isScheduled(pos, this)) {
            tickScheduler.schedule(pos, this, 1);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        TickScheduler<Block> tickScheduler = world.getBlockTickScheduler();

        if (!tickScheduler.isScheduled(pos, this)) {
            tickScheduler.schedule(pos, this, 1);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        TickScheduler<Block> tickScheduler = world.getBlockTickScheduler();

        if (!tickScheduler.isScheduled(pos, this)) {
            tickScheduler.schedule(pos, this, 1);
        }
    }
}

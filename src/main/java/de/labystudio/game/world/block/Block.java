package de.labystudio.game.world.block;

import de.labystudio.game.render.world.IWorldAccess;
import de.labystudio.game.util.BoundingBox;
import de.labystudio.game.util.EnumBlockFace;
import de.labystudio.game.world.WorldRenderer;

import java.util.HashMap;
import java.util.Map;

public abstract class Block {

    private static final Map<Short, Block> blocks = new HashMap<>();

    public static BlockStone STONE = new BlockStone(1, 0);
    public static BlockGrass GRASS = new BlockGrass(2, 1);
    public static BlockDirt DIRT = new BlockDirt(3, 2);
    public static BlockLog LOG = new BlockLog(17, 4);
    public static BlockLeave LEAVE = new BlockLeave(18, 6);
    public static BlockWater WATER = new BlockWater(9, 7);
    public static BlockSand SAND = new BlockSand(12, 8);

    protected final int id;
    protected final int textureSlotId;

    // Block bounding box
    protected BoundingBox boundingBox = new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

    protected Block(int id) {
        this(id, id);
    }

    protected Block(int id, int textureSlotId) {
        this.id = id;
        this.textureSlotId = textureSlotId;
        blocks.put((short) id, this);
    }

    public static Block getById(short typeId) {
        return blocks.get(typeId);
    }

    public int getId() {
        return id;
    }

    public int getTextureForFace(EnumBlockFace face) {
        return this.textureSlotId;
    }

    public boolean isTransparent() {
        return getOpacity() < 1.0F;
    }

    public boolean shouldRenderFace(IWorldAccess world, int x, int y, int z, EnumBlockFace face) {
        short typeId = world.getBlockAt(x + face.x, y + face.y, z + face.z);
        return typeId == 0 || Block.getById(typeId).isTransparent();
    }

    public boolean isSolid() {
        return true;
    }

    public float getOpacity() {
        return 1.0F;
    }

    public BoundingBox getBoundingBox(IWorldAccess world, int x, int y, int z) {
        return this.boundingBox;
    }

    public void render(WorldRenderer worldRenderer, IWorldAccess world, int x, int y, int z) {
        worldRenderer.getBlockRenderer().renderBlock(world, this, x, y, z);
    }
}

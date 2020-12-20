package de.labystudio.game.world.chunk.format;


import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.*;
import de.labystudio.game.world.World;
import de.labystudio.game.world.block.Block;
import de.labystudio.game.world.chunk.Chunk;
import de.labystudio.game.world.chunk.ChunkLayers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChunkFormat {

    private Chunk[] chunks = new Chunk[16];

    private World world;
    private int x;
    private int z;

    private boolean empty = true;

    public ChunkFormat(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public ChunkFormat read(DataInputStream inputStream, int chunkX, int chunkZ) throws IOException {
        if (this.empty = inputStream == null)
            return this;

        // Get root tag
        CompoundTag rootTag = (CompoundTag) NBTIO.readTag((InputStream) inputStream);

        // Load chunk format
        CompoundTag level = rootTag.get("Level");
        ListTag sections = level.get("Sections");

        for (int i = 0; i < sections.size(); i++) {
            CompoundTag section = sections.get(i);

            try {
                byte y = (byte) section.get("Y").getValue();

                byte[] blocks = ((ByteArrayTag) section.get("Blocks")).getValue();
                byte[] add = section.contains("Add") ? ((ByteArrayTag) section.get("Add")).getValue() : new byte[blocks.length];
                byte[] blockLight = ((ByteArrayTag) section.get("BlockLight")).getValue();
                byte[] skyLight = ((ByteArrayTag) section.get("SkyLight")).getValue();
                // byte[] data = ((ByteArrayTag) section.get("Data")).getValue();

                Chunk chunk = new Chunk(this.world, chunkX, y, chunkZ);

                for (int relY = 0; relY < 16; relY++) {
                    for (int relX = 0; relX < 16; relX++) {
                        for (int relZ = 0; relZ < 16; relZ++) {
                            int index = (relY * 16 + relZ) * 16 + relX;
                            int blockId = ((add[index] & 0xFF) << 4) | (blocks[index] & 0xFF);
                            //int typeAndData = (add[index] << 8) | blockId | getHalfByte(index, data);

                            // Combine sky light and block light
                            int lightLevel = Math.max(getHalfByte(index, blockLight), getHalfByte(index, skyLight));

                            if (blockId != 0) {
                                this.empty = false;
                            }

                            // Invalid block, convert to stone
                            if (blockId != 0 && Block.getById((short) blockId) == null) {
                                blockId = Block.STONE.getId();
                            }

                            chunk.setBlockAt(relX, relY, relZ, blockId);
                            chunk.setLightAt(relX, relY, relZ, lightLevel);
                        }
                    }
                }


                this.chunks[y] = chunk;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    private static byte getHalfByte(int index, byte[] bytes) {
        return (byte) ((bytes[index / 2] >> (index % 2 == 0 ? 0 : 4)) & 0xF);
    }

    private static void setHalfByte(int index, byte value, byte[] bytes) {
        bytes[index / 2] = (byte) ((bytes[index / 2] &= 0xF << (index % 2 == 0 ? 4 : 0)) | (value & 0xF) << (index % 2 == 0 ? 0 : 4));
    }

    public static void write(ChunkLayers chunkLayers, DataOutputStream dataOutputStream) throws IOException {
        List<Tag> sectionList = new ArrayList<Tag>();
        for (byte y = 0; y < 16; y++) {
            Chunk chunkSection = chunkLayers.getLayer(y);

            // Skip empty chunks
            if (chunkSection == null || chunkSection.isEmpty()) {
                continue;
            }

            // Section data
            CompoundTag section = new CompoundTag("");

            // Set Y tag
            section.put(new ByteTag("Y", y));

            // Content
            ByteArrayTag blocks = new ByteArrayTag("Blocks");
            ByteArrayTag add = new ByteArrayTag("Add");
            ByteArrayTag data = new ByteArrayTag("Data");
            ByteArrayTag blockLight = new ByteArrayTag("BlockLight");
            ByteArrayTag skyLight = new ByteArrayTag("SkyLight");

            byte[] blockArray = new byte[4096];
            byte[] addArray = new byte[4096];
            byte[] lightArray = new byte[4096];
            byte[] skyLightArray = new byte[4096];
            //byte[] dataArray = new byte[4096];

            for (int relY = 0; relY < 16; relY++) {
                for (int relX = 0; relX < 16; relX++) {
                    for (int relZ = 0; relZ < 16; relZ++) {
                        int index = (relY * 16 + relZ) * 16 + relX;

                        int blockId = chunkSection.getBlockAt(relX, relY, relZ);
                        int blockLightShort = chunkSection.getLightAt(relX, relY, relZ);

                        blockArray[index] = (byte) (blockId & 0xFF);
                        addArray[index] = (byte) (blockId >> 4);
                        setHalfByte(index, (byte) blockLightShort, lightArray);

                        // int typeAndData = chunkSection.getBlockAt(relX, relY, relZ);
                        // blockArray[index] = (byte) ((typeAndData & 0xFF) >> 4);
                        // addArray[index] = (byte) (typeAndData >> 8);
                        // setHalfByte(index, (byte) typeAndData, dataArray);
                    }
                }
            }

            // Fill content tags
            blocks.setValue(blockArray);
            add.setValue(addArray);
            blockLight.setValue(lightArray);
            skyLight.setValue(skyLightArray);
            // data.setValue(dataArray);

            // Add to section
            section.put(blocks);
            section.put(add);
            //section.put(data);
            section.put(blockLight);
            section.put(skyLight);

            // Add section to list
            sectionList.add(section);
        }

        // Add level tags
        CompoundTag level = new CompoundTag("Level");
        level.put(new ListTag("Sections", sectionList));

        // Create root
        CompoundTag root = new CompoundTag("");

        // Add level to root
        root.put(level);

        // Write
        NBTIO.writeTag((OutputStream) dataOutputStream, root);
    }

    public Chunk[] getChunks() {
        return chunks;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isEmpty() {
        return empty;
    }
}
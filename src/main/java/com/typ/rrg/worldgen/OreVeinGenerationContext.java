package com.typ.rrg.worldgen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.chunk.ChunkAccess;

public final class OreVeinGenerationContext {
    private static final Map<ChunkAccess, ResolvedOreGate> CHUNK_GATES = new ConcurrentHashMap<>();
    private static final ThreadLocal<Deque<Boolean>> ENTERED_NOISE_CHUNKS = new ThreadLocal<>();
    private static final ThreadLocal<Deque<ResolvedOreGate>> CURRENT_GATES = new ThreadLocal<>();

    private OreVeinGenerationContext() {
    }

    public static void registerChunk(ChunkAccess chunk, ResolvedOreGate gate) {
        if (gate == null) {
            CHUNK_GATES.remove(chunk);
        } else {
            CHUNK_GATES.put(chunk, gate);
        }
    }

    public static void unregisterChunk(ChunkAccess chunk) {
        CHUNK_GATES.remove(chunk);
    }

    public static void enterNoiseChunk(ChunkAccess chunk) {
        Deque<Boolean> entries = ENTERED_NOISE_CHUNKS.get();
        if (entries == null) {
            entries = new ArrayDeque<>();
            ENTERED_NOISE_CHUNKS.set(entries);
        }

        ResolvedOreGate gate = CHUNK_GATES.get(chunk);
        if (gate != null) {
            entries.push(Boolean.TRUE);

            Deque<ResolvedOreGate> gates = CURRENT_GATES.get();
            if (gates == null) {
                gates = new ArrayDeque<>();
                CURRENT_GATES.set(gates);
            }
            gates.push(gate);
        } else {
            entries.push(Boolean.FALSE);
        }
    }

    public static void exitNoiseChunk(ChunkAccess chunk) {
        Deque<Boolean> entries = ENTERED_NOISE_CHUNKS.get();
        if (entries == null || entries.isEmpty()) {
            return;
        }

        boolean pushedDimension = entries.pop();
        if (entries.isEmpty()) {
            ENTERED_NOISE_CHUNKS.remove();
        }
        if (!pushedDimension) {
            return;
        }

        Deque<ResolvedOreGate> gates = CURRENT_GATES.get();
        if (gates != null && !gates.isEmpty()) {
            gates.pop();
        }
        if (gates == null || gates.isEmpty()) {
            CURRENT_GATES.remove();
        }
    }

    public static ResolvedOreGate currentGate() {
        Deque<ResolvedOreGate> gates = CURRENT_GATES.get();
        return gates == null ? null : gates.peek();
    }
}

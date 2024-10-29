package com.dylibso.chicory.aot;

import com.dylibso.chicory.runtime.OpcodeImpl;
import java.util.Map;

final class AotEmitterMap {

    public static final Map<AotOpCode, AotEmitters.BytecodeEmitter> EMITTERS =
            AotEmitters.builder()
                    // ====== Misc ======
                    .intrinsic(AotOpCode.DROP_KEEP, AotEmitters::DROP_KEEP)
                    .intrinsic(AotOpCode.TRAP, AotEmitters::TRAP)
                    .intrinsic(AotOpCode.RETURN, AotEmitters::RETURN)
                    .intrinsic(AotOpCode.DROP, AotEmitters::DROP)
                    .intrinsic(AotOpCode.ELEM_DROP, AotEmitters::ELEM_DROP)
                    .intrinsic(AotOpCode.SELECT, AotEmitters::SELECT)

                    // ====== Control Flow ======
                    .intrinsic(AotOpCode.CALL, AotEmitters::CALL)
                    .intrinsic(AotOpCode.CALL_INDIRECT, AotEmitters::CALL_INDIRECT)

                    // ====== References ======
                    .intrinsic(AotOpCode.REF_FUNC, AotEmitters::REF_FUNC)
                    .intrinsic(AotOpCode.REF_NULL, AotEmitters::REF_NULL)
                    .intrinsic(AotOpCode.REF_IS_NULL, AotEmitters::REF_IS_NULL)

                    // ====== Locals & Globals ======
                    .intrinsic(AotOpCode.LOCAL_GET, AotEmitters::LOCAL_GET)
                    .intrinsic(AotOpCode.LOCAL_SET, AotEmitters::LOCAL_SET)
                    .intrinsic(AotOpCode.LOCAL_TEE, AotEmitters::LOCAL_TEE)
                    .intrinsic(AotOpCode.GLOBAL_GET, AotEmitters::GLOBAL_GET)
                    .intrinsic(AotOpCode.GLOBAL_SET, AotEmitters::GLOBAL_SET)

                    // ====== Tables ======
                    .intrinsic(AotOpCode.TABLE_GET, AotEmitters::TABLE_GET)
                    .intrinsic(AotOpCode.TABLE_SET, AotEmitters::TABLE_SET)
                    .intrinsic(AotOpCode.TABLE_SIZE, AotEmitters::TABLE_SIZE)
                    .intrinsic(AotOpCode.TABLE_GROW, AotEmitters::TABLE_GROW)
                    .intrinsic(AotOpCode.TABLE_FILL, AotEmitters::TABLE_FILL)
                    .intrinsic(AotOpCode.TABLE_COPY, AotEmitters::TABLE_COPY)
                    .intrinsic(AotOpCode.TABLE_INIT, AotEmitters::TABLE_INIT)

                    // ====== Memory ======
                    .intrinsic(AotOpCode.MEMORY_INIT, AotEmitters::MEMORY_INIT)
                    .intrinsic(AotOpCode.MEMORY_COPY, AotEmitters::MEMORY_COPY)
                    .intrinsic(AotOpCode.MEMORY_FILL, AotEmitters::MEMORY_FILL)
                    .intrinsic(AotOpCode.MEMORY_GROW, AotEmitters::MEMORY_GROW)
                    .intrinsic(AotOpCode.MEMORY_SIZE, AotEmitters::MEMORY_SIZE)
                    .intrinsic(AotOpCode.DATA_DROP, AotEmitters::DATA_DROP)

                    // ====== Load & Store ======
                    .intrinsic(AotOpCode.I32_LOAD, AotEmitters::I32_LOAD)
                    .intrinsic(AotOpCode.I32_LOAD8_S, AotEmitters::I32_LOAD8_S)
                    .intrinsic(AotOpCode.I32_LOAD8_U, AotEmitters::I32_LOAD8_U)
                    .intrinsic(AotOpCode.I32_LOAD16_S, AotEmitters::I32_LOAD16_S)
                    .intrinsic(AotOpCode.I32_LOAD16_U, AotEmitters::I32_LOAD16_U)
                    .intrinsic(AotOpCode.I64_LOAD, AotEmitters::I64_LOAD)
                    .intrinsic(AotOpCode.I64_LOAD8_S, AotEmitters::I64_LOAD8_S)
                    .intrinsic(AotOpCode.I64_LOAD8_U, AotEmitters::I64_LOAD8_U)
                    .intrinsic(AotOpCode.I64_LOAD16_S, AotEmitters::I64_LOAD16_S)
                    .intrinsic(AotOpCode.I64_LOAD16_U, AotEmitters::I64_LOAD16_U)
                    .intrinsic(AotOpCode.I64_LOAD32_S, AotEmitters::I64_LOAD32_S)
                    .intrinsic(AotOpCode.I64_LOAD32_U, AotEmitters::I64_LOAD32_U)
                    .intrinsic(AotOpCode.F32_LOAD, AotEmitters::F32_LOAD)
                    .intrinsic(AotOpCode.F64_LOAD, AotEmitters::F64_LOAD)
                    .intrinsic(AotOpCode.I32_STORE, AotEmitters::I32_STORE)
                    .intrinsic(AotOpCode.I32_STORE8, AotEmitters::I32_STORE8)
                    .intrinsic(AotOpCode.I32_STORE16, AotEmitters::I32_STORE16)
                    .intrinsic(AotOpCode.I64_STORE, AotEmitters::I64_STORE)
                    .intrinsic(AotOpCode.I64_STORE8, AotEmitters::I64_STORE8)
                    .intrinsic(AotOpCode.I64_STORE16, AotEmitters::I64_STORE16)
                    .intrinsic(AotOpCode.I64_STORE32, AotEmitters::I64_STORE32)
                    .intrinsic(AotOpCode.F32_STORE, AotEmitters::F32_STORE)
                    .intrinsic(AotOpCode.F64_STORE, AotEmitters::F64_STORE)

                    // ====== I32 ======
                    .intrinsic(AotOpCode.I32_ADD, AotEmitters::I32_ADD)
                    .intrinsic(AotOpCode.I32_AND, AotEmitters::I32_AND)
                    .shared(AotOpCode.I32_CLZ, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I32_CONST, AotEmitters::I32_CONST)
                    .shared(AotOpCode.I32_CTZ, OpcodeImpl.class)
                    .shared(AotOpCode.I32_DIV_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_DIV_U, OpcodeImpl.class)
                    .shared(AotOpCode.I32_EQ, OpcodeImpl.class)
                    .shared(AotOpCode.I32_EQZ, OpcodeImpl.class)
                    .shared(AotOpCode.I32_EXTEND_8_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_EXTEND_16_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_GE_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_GE_U, OpcodeImpl.class)
                    .shared(AotOpCode.I32_GT_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_GT_U, OpcodeImpl.class)
                    .shared(AotOpCode.I32_LE_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_LE_U, OpcodeImpl.class)
                    .shared(AotOpCode.I32_LT_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_LT_U, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I32_MUL, AotEmitters::I32_MUL)
                    .shared(AotOpCode.I32_NE, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I32_OR, AotEmitters::I32_OR)
                    .shared(AotOpCode.I32_POPCNT, OpcodeImpl.class)
                    .shared(AotOpCode.I32_REINTERPRET_F32, OpcodeImpl.class)
                    .shared(AotOpCode.I32_REM_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_REM_U, OpcodeImpl.class)
                    .shared(AotOpCode.I32_ROTL, OpcodeImpl.class)
                    .shared(AotOpCode.I32_ROTR, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I32_SHL, AotEmitters::I32_SHL)
                    .intrinsic(AotOpCode.I32_SHR_S, AotEmitters::I32_SHR_S)
                    .intrinsic(AotOpCode.I32_SHR_U, AotEmitters::I32_SHR_U)
                    .intrinsic(AotOpCode.I32_SUB, AotEmitters::I32_SUB)
                    .shared(AotOpCode.I32_TRUNC_F32_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_TRUNC_F32_U, OpcodeImpl.class)
                    .shared(AotOpCode.I32_TRUNC_F64_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_TRUNC_F64_U, OpcodeImpl.class)
                    .shared(AotOpCode.I32_TRUNC_SAT_F32_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_TRUNC_SAT_F32_U, OpcodeImpl.class)
                    .shared(AotOpCode.I32_TRUNC_SAT_F64_S, OpcodeImpl.class)
                    .shared(AotOpCode.I32_TRUNC_SAT_F64_U, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I32_WRAP_I64, AotEmitters::I32_WRAP_I64)
                    .intrinsic(AotOpCode.I32_XOR, AotEmitters::I32_XOR)

                    // ====== I64 ======
                    .intrinsic(AotOpCode.I64_ADD, AotEmitters::I64_ADD)
                    .intrinsic(AotOpCode.I64_AND, AotEmitters::I64_AND)
                    .shared(AotOpCode.I64_CLZ, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I64_CONST, AotEmitters::I64_CONST)
                    .shared(AotOpCode.I64_CTZ, OpcodeImpl.class)
                    .shared(AotOpCode.I64_DIV_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_DIV_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_EQ, OpcodeImpl.class)
                    .shared(AotOpCode.I64_EQZ, OpcodeImpl.class)
                    .shared(AotOpCode.I64_EXTEND_8_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_EXTEND_16_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_EXTEND_32_S, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I64_EXTEND_I32_S, AotEmitters::I64_EXTEND_I32_S)
                    .shared(AotOpCode.I64_EXTEND_I32_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_GE_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_GE_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_GT_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_GT_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_LE_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_LE_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_LT_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_LT_U, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I64_MUL, AotEmitters::I64_MUL)
                    .shared(AotOpCode.I64_NE, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I64_OR, AotEmitters::I64_OR)
                    .shared(AotOpCode.I64_POPCNT, OpcodeImpl.class)
                    .shared(AotOpCode.I64_REM_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_REM_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_ROTL, OpcodeImpl.class)
                    .shared(AotOpCode.I64_ROTR, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I64_SHL, AotEmitters::I64_SHL)
                    .intrinsic(AotOpCode.I64_SHR_S, AotEmitters::I64_SHR_S)
                    .intrinsic(AotOpCode.I64_SHR_U, AotEmitters::I64_SHR_U)
                    .intrinsic(AotOpCode.I64_SUB, AotEmitters::I64_SUB)
                    .shared(AotOpCode.I64_REINTERPRET_F64, OpcodeImpl.class)
                    .shared(AotOpCode.I64_TRUNC_F32_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_TRUNC_F32_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_TRUNC_F64_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_TRUNC_F64_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_TRUNC_SAT_F32_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_TRUNC_SAT_F32_U, OpcodeImpl.class)
                    .shared(AotOpCode.I64_TRUNC_SAT_F64_S, OpcodeImpl.class)
                    .shared(AotOpCode.I64_TRUNC_SAT_F64_U, OpcodeImpl.class)
                    .intrinsic(AotOpCode.I64_XOR, AotEmitters::I64_XOR)

                    // ====== F32 ======
                    .shared(AotOpCode.F32_ABS, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F32_ADD, AotEmitters::F32_ADD)
                    .shared(AotOpCode.F32_CEIL, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F32_CONST, AotEmitters::F32_CONST)
                    .shared(AotOpCode.F32_CONVERT_I32_S, OpcodeImpl.class)
                    .shared(AotOpCode.F32_CONVERT_I32_U, OpcodeImpl.class)
                    .shared(AotOpCode.F32_CONVERT_I64_S, OpcodeImpl.class)
                    .shared(AotOpCode.F32_CONVERT_I64_U, OpcodeImpl.class)
                    .shared(AotOpCode.F32_COPYSIGN, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F32_DEMOTE_F64, AotEmitters::F32_DEMOTE_F64)
                    .intrinsic(AotOpCode.F32_DIV, AotEmitters::F32_DIV)
                    .shared(AotOpCode.F32_EQ, OpcodeImpl.class)
                    .shared(AotOpCode.F32_FLOOR, OpcodeImpl.class)
                    .shared(AotOpCode.F32_GE, OpcodeImpl.class)
                    .shared(AotOpCode.F32_GT, OpcodeImpl.class)
                    .shared(AotOpCode.F32_LE, OpcodeImpl.class)
                    .shared(AotOpCode.F32_LT, OpcodeImpl.class)
                    .shared(AotOpCode.F32_MAX, OpcodeImpl.class)
                    .shared(AotOpCode.F32_MIN, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F32_MUL, AotEmitters::F32_MUL)
                    .shared(AotOpCode.F32_NE, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F32_NEG, AotEmitters::F32_NEG)
                    .shared(AotOpCode.F32_NEAREST, OpcodeImpl.class)
                    .shared(AotOpCode.F32_REINTERPRET_I32, OpcodeImpl.class)
                    .shared(AotOpCode.F32_SQRT, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F32_SUB, AotEmitters::F32_SUB)
                    .shared(AotOpCode.F32_TRUNC, OpcodeImpl.class)

                    // ====== F64 ======
                    .shared(AotOpCode.F64_ABS, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F64_ADD, AotEmitters::F64_ADD)
                    .shared(AotOpCode.F64_CEIL, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F64_CONST, AotEmitters::F64_CONST)
                    .shared(AotOpCode.F64_CONVERT_I32_S, OpcodeImpl.class)
                    .shared(AotOpCode.F64_CONVERT_I32_U, OpcodeImpl.class)
                    .shared(AotOpCode.F64_CONVERT_I64_S, OpcodeImpl.class)
                    .shared(AotOpCode.F64_CONVERT_I64_U, OpcodeImpl.class)
                    .shared(AotOpCode.F64_COPYSIGN, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F64_DIV, AotEmitters::F64_DIV)
                    .shared(AotOpCode.F64_EQ, OpcodeImpl.class)
                    .shared(AotOpCode.F64_FLOOR, OpcodeImpl.class)
                    .shared(AotOpCode.F64_GE, OpcodeImpl.class)
                    .shared(AotOpCode.F64_GT, OpcodeImpl.class)
                    .shared(AotOpCode.F64_LE, OpcodeImpl.class)
                    .shared(AotOpCode.F64_LT, OpcodeImpl.class)
                    .shared(AotOpCode.F64_MAX, OpcodeImpl.class)
                    .shared(AotOpCode.F64_MIN, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F64_MUL, AotEmitters::F64_MUL)
                    .shared(AotOpCode.F64_NE, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F64_NEG, AotEmitters::F64_NEG)
                    .shared(AotOpCode.F64_NEAREST, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F64_PROMOTE_F32, AotEmitters::F64_PROMOTE_F32)
                    .shared(AotOpCode.F64_REINTERPRET_I64, OpcodeImpl.class)
                    .shared(AotOpCode.F64_SQRT, OpcodeImpl.class)
                    .intrinsic(AotOpCode.F64_SUB, AotEmitters::F64_SUB)
                    .shared(AotOpCode.F64_TRUNC, OpcodeImpl.class)
                    .build();

    private AotEmitterMap() {}
}

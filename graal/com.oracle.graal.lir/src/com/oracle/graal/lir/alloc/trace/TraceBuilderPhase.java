/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.lir.alloc.trace;

import static com.oracle.graal.lir.alloc.trace.TraceUtil.isTrivialTrace;

import java.util.List;

import com.oracle.graal.compiler.common.alloc.BiDirectionalTraceBuilder;
import com.oracle.graal.compiler.common.alloc.SingleBlockTraceBuilder;
import com.oracle.graal.compiler.common.alloc.Trace;
import com.oracle.graal.compiler.common.alloc.TraceBuilderResult;
import com.oracle.graal.compiler.common.alloc.TraceBuilderResult.TrivialTracePredicate;
import com.oracle.graal.compiler.common.alloc.TraceStatisticsPrinter;
import com.oracle.graal.compiler.common.alloc.UniDirectionalTraceBuilder;
import com.oracle.graal.compiler.common.cfg.AbstractBlockBase;
import com.oracle.graal.debug.Debug;
import com.oracle.graal.debug.GraalError;
import com.oracle.graal.lir.LIR;
import com.oracle.graal.lir.gen.LIRGenerationResult;
import com.oracle.graal.lir.phases.AllocationPhase;
import com.oracle.graal.options.EnumOptionValue;
import com.oracle.graal.options.Option;
import com.oracle.graal.options.OptionType;
import com.oracle.graal.options.OptionValue;

import jdk.vm.ci.code.TargetDescription;

public class TraceBuilderPhase extends AllocationPhase {

    public enum TraceBuilder {
        UniDirectional,
        BiDirectional,
        SingleBlock
    }

    public static class Options {
        // @formatter:off
        @Option(help = "Trace building algorithm.", type = OptionType.Debug)
        public static final EnumOptionValue<TraceBuilder> TraceBuilding = new EnumOptionValue<>(TraceBuilder.UniDirectional);
        @Option(help = "Schedule trivial traces as early as possible.", type = OptionType.Debug)
        public static final OptionValue<Boolean> TraceRAScheduleTrivialTracesEarly = new OptionValue<>(true);
        // @formatter:on
    }

    private static final int TRACE_LOG_LEVEL = 1;
    public static final int TRACE_DUMP_LEVEL = 3;

    @Override
    protected void run(TargetDescription target, LIRGenerationResult lirGenRes, AllocationContext context) {
        AbstractBlockBase<?>[] linearScanOrder = lirGenRes.getLIR().linearScanOrder();
        AbstractBlockBase<?> startBlock = linearScanOrder[0];
        LIR lir = lirGenRes.getLIR();
        assert startBlock.equals(lir.getControlFlowGraph().getStartBlock());

        final TraceBuilderResult traceBuilderResult = getTraceBuilderResult(lir, startBlock, linearScanOrder);

        if (Debug.isLogEnabled(TRACE_LOG_LEVEL)) {
            List<Trace> traces = traceBuilderResult.getTraces();
            for (int i = 0; i < traces.size(); i++) {
                Trace trace = traces.get(i);
                Debug.log(TRACE_LOG_LEVEL, "Trace %5d: %s%s", i, trace, isTrivialTrace(lirGenRes.getLIR(), trace) ? " (trivial)" : "");
            }
        }
        TraceStatisticsPrinter.printTraceStatistics(traceBuilderResult, lirGenRes.getCompilationUnitName());
        Debug.dump(TRACE_DUMP_LEVEL, traceBuilderResult, "After TraceBuilding");
        context.contextAdd(traceBuilderResult);
    }

    private static TraceBuilderResult getTraceBuilderResult(LIR lir, AbstractBlockBase<?> startBlock, AbstractBlockBase<?>[] linearScanOrder) {
        TraceBuilderResult.TrivialTracePredicate pred = getTrivialTracePredicate(lir);

        TraceBuilder selectedTraceBuilder = Options.TraceBuilding.getValue();
        Debug.log(TRACE_LOG_LEVEL, "Building Traces using %s", selectedTraceBuilder);
        switch (Options.TraceBuilding.getValue()) {
            case SingleBlock:
                return SingleBlockTraceBuilder.computeTraces(startBlock, linearScanOrder, pred);
            case BiDirectional:
                return BiDirectionalTraceBuilder.computeTraces(startBlock, linearScanOrder, pred);
            case UniDirectional:
                return UniDirectionalTraceBuilder.computeTraces(startBlock, linearScanOrder, pred);
        }
        throw GraalError.shouldNotReachHere("Unknown trace building algorithm: " + Options.TraceBuilding.getValue());
    }

    public static TraceBuilderResult.TrivialTracePredicate getTrivialTracePredicate(LIR lir) {
        if (!Options.TraceRAScheduleTrivialTracesEarly.getValue()) {
            return null;
        }
        return new TrivialTracePredicate() {
            @Override
            public boolean isTrivialTrace(Trace trace) {
                return TraceUtil.isTrivialTrace(lir, trace);
            }
        };
    }
}

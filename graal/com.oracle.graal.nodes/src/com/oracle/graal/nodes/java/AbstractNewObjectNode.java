/*
 * Copyright (c) 2009, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.nodes.java;

import static com.oracle.graal.nodeinfo.NodeCycles.CYCLES_20;
import static com.oracle.graal.nodeinfo.NodeSize.SIZE_20;

import com.oracle.graal.compiler.common.type.Stamp;
import com.oracle.graal.graph.NodeClass;
import com.oracle.graal.nodeinfo.NodeInfo;
import com.oracle.graal.nodes.DeoptimizingFixedWithNextNode;
import com.oracle.graal.nodes.FrameState;
import com.oracle.graal.nodes.spi.Lowerable;
import com.oracle.graal.nodes.spi.LoweringTool;

/**
 * The {@code AbstractNewObjectNode} is the base class for the new instance and new array nodes.
 */
@NodeInfo(cycles = CYCLES_20, size = SIZE_20)
public abstract class AbstractNewObjectNode extends DeoptimizingFixedWithNextNode implements Lowerable {

    public static final NodeClass<AbstractNewObjectNode> TYPE = NodeClass.create(AbstractNewObjectNode.class);
    protected final boolean fillContents;

    protected AbstractNewObjectNode(NodeClass<? extends AbstractNewObjectNode> c, Stamp stamp, boolean fillContents, FrameState stateBefore) {
        super(c, stamp, stateBefore);
        this.fillContents = fillContents;
    }

    /**
     * @return <code>true</code> if the object's contents should be initialized to zero/null.
     */
    public boolean fillContents() {
        return fillContents;
    }

    @Override
    public void lower(LoweringTool tool) {
        tool.getLowerer().lower(this, tool);
    }

    @Override
    public boolean canDeoptimize() {
        return true;
    }
}

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
package com.oracle.graal.replacements.nodes;

import static com.oracle.graal.nodeinfo.NodeCycles.CYCLES_0;
import static com.oracle.graal.nodeinfo.NodeSize.SIZE_0;

import com.oracle.graal.api.replacements.Snippet.VarargsParameter;
import com.oracle.graal.compiler.common.type.Stamp;
import com.oracle.graal.graph.Node;
import com.oracle.graal.graph.NodeClass;
import com.oracle.graal.graph.NodeInputList;
import com.oracle.graal.graph.spi.Canonicalizable;
import com.oracle.graal.graph.spi.CanonicalizerTool;
import com.oracle.graal.nodeinfo.NodeInfo;
import com.oracle.graal.nodes.FixedWithNextNode;
import com.oracle.graal.nodes.ParameterNode;
import com.oracle.graal.nodes.ValueNode;

/**
 * Implements the semantics of {@link VarargsParameter}.
 */
@NodeInfo(cycles = CYCLES_0, size = SIZE_0)
public final class LoadSnippetVarargParameterNode extends FixedWithNextNode implements Canonicalizable {

    public static final NodeClass<LoadSnippetVarargParameterNode> TYPE = NodeClass.create(LoadSnippetVarargParameterNode.class);
    @Input ValueNode index;

    @Input NodeInputList<ParameterNode> parameters;

    public LoadSnippetVarargParameterNode(ParameterNode[] locals, ValueNode index, Stamp stamp) {
        super(TYPE, stamp);
        this.index = index;
        this.parameters = new NodeInputList<>(this, locals);
    }

    @Override
    public Node canonical(CanonicalizerTool tool) {
        if (index.isConstant()) {
            int indexValue = index.asJavaConstant().asInt();
            if (indexValue < parameters.size()) {
                return parameters.get(indexValue);
            }
        }
        return this;
    }
}

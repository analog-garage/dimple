/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

/**
 * Proposal kernel support.
 * <p>
 * Proposal kernels are used in sampling. They represent functions that given the value
 * of one or more variables will generate a proposed new sample value that fit some distribution
 * defined by the parameters of the proposal kernel. There are two kinds of kernels:
 * <ol>
 * <li>Single-value kernels generate proposals for a single sample value and
 * implement the interface {@link com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel IProposalKernel}.
 * <li>Block kernels generate proposals for multiple values simultaneously and implement the interface
 * {@link com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel IBlockProposalKernel}
 * </ol>
 * Some proposal kernel implementations can be configured with options. Currently this includes the classes:
 * <ul>
 * <li>{@linkplain com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel NormalProposalKernel}
 * <li>{@linkplain com.analog.lyric.dimple.solvers.core.proposalKernels.CircularNormalProposalKernel
 * CircularNormalProposalKernel}
 * </ul>
 * When that is the case, the relevant option keys are defined in static public fields within the implementation
 * classes and the kernel can be configured by setting the option on the variable for that
 * kernel instance (or on the graph itself if all variables in the graph will share the same parameters).
 */
@NonNullByDefault
package com.analog.lyric.dimple.solvers.core.proposalKernels;
import org.eclipse.jdt.annotation.NonNullByDefault;


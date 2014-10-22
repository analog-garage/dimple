/*******************************************************************************
 *   Copyright 2014 Analog Devices, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.jsproxy;

import netscape.javascript.JSException;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault(false)
public interface IJSObject
{

	public abstract Object call(String functionName, Object[] args) throws JSException;

	public abstract Object eval(String arg0) throws JSException;

	public abstract Object getMember(String name) throws JSException;

	public abstract Object getSlot(int i) throws JSException;

	public abstract void removeMember(String name) throws JSException;

	public abstract void setMember(String name, Object value) throws JSException;

	public abstract void setSlot(int i, Object value) throws JSException;

}

package com.apple.mrj.jdirect;

/**
 * This is a simple stub for this class which is specific to the 1.3.1
 * virtual machine from Apple. It allows developers to compile the
 * MRJ Adapter library on platforms other than Mac OS X with the 1.3.1 VM.
 * You don't need to use these stubs if you're using the precompiled
 * version of MRJ Adapter.
 *
 * @author Steve Roy
 */
public class MethodClosure
{
	protected MethodClosure(Object targetObject, String methodName,
		String methodSignature)
	{
	}
	
	public int getProc()
	{
		return -1;
	}
}

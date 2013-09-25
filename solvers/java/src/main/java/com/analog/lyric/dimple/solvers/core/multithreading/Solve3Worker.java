package com.analog.lyric.dimple.solvers.core.multithreading;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class Solve3Worker 
{
//
//	
//	public class Solve3Worker implements Callable
//	{
//		//private LinkedBlockingDeque<DependencyGraphNode> [] _doneQueues;
//		private LinkedBlockingDeque<NewDependencyGraphNode> [] _workQueues;
//		private int _me;
//		private AtomicInteger _numNodes;
//		
//		public Solve3Worker(LinkedBlockingDeque<NewDependencyGraphNode> [] workQueues,
//				int me //,
////				AtomicInteger numNodes
//				)
//		{
//			_workQueues = workQueues;
//			_me = me;
//	//		_numNodes = numNodes;
//		}
//		
//		@Override
//		public Object call() throws Exception 
//		{
//			NewDependencyGraphNode dgn = _workQueues[_me].poll();
//
//			while (dgn != null)
//			{
//				//DependencyGraphNode dgn = _workQueue.take();
//
//				
//				if (dgn instanceof Poison)
//					break;
//				
////				int value = _numNodes.decrementAndGet();
////				
////				if (value == 0)
////				{
////					for (int i = 0; i < _workQueues.length; i++)
////						_workQueues[i].add(new Poison());
////				}
//				
//				dgn.scheduleEntry.update();
//				
//				for (int i = 0; i < dgn.dependents.size(); i++)
//				{
////					DependencyGraphNode dependent = dgn.dependents.get(i);
////					if (dependent.decrementAndCheckReady())
////					{
////						_workQueues[_me].add(dgn.dependents.get(i));
////					}
//				}
//				//_doneQueue.add(dgn);
//				dgn = _workQueues[_me].poll();
//			}
//			
//			
//			return null;
//		}
//	}
}

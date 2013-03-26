package scjlibs;

import scjlibs.util.PoolObject;
import scjlibs.util.PoolObjectFactory;

public class GenericEntryFactory implements PoolObjectFactory {

	@Override
	public PoolObject createObject() {
		// TODO Auto-generated method stub
		return new GenericEntry();
	}

}

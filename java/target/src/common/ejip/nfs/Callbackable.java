package ejip.nfs;

import ejip.nfs.datastructs.ResultType;

public interface Callbackable {
	public abstract void callback(ResultType message);
}

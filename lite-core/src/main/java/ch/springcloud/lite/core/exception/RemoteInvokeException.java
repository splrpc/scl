package ch.springcloud.lite.core.exception;

public class RemoteInvokeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6660248666031239869L;

	public RemoteInvokeException() {
		super();
	}

	public RemoteInvokeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RemoteInvokeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemoteInvokeException(String message) {
		super(message);
	}

	public RemoteInvokeException(Throwable cause) {
		super(cause);
	}

	
	
}

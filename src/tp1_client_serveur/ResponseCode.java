package tp1_client_serveur;

public class ResponseCode{
	 public static final int INVALID_COMMAND = 55555;
	 
	 public static final int LIST_FILE_OK = 119;
	 public static final int LIST_FILE_ERROR = 911;
	 
	 public static final int MAKE_DIR_OK = 007;
	 public static final int MAKE_DIR_ERROR = 700;
	 
	 public static final int DOWNLOAD_FILE_OK = 696969;
	 public static final int DOWNLOAD_FILE_ERROR = 969696;
	 
	 public static final int UPLOAD_FILE_READY = 0140;
	 public static final int UPLOAD_FILE_ERROR = 0410;
	 
	 public static final int CHANGE_DIR_OK = 420;
	 public static final int CHANGE_DIR_FAIL = 024;
	 
	 public static final int EXIT_OK = 99;
	
}

package com.redhat.installer.installation.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import java.io.IOException;
import java.net.*;


public class NoPortClashValidator implements DataValidator {

	private String message;
    private String error;
    private String warning;

	public Status validateData(AutomatedInstallData idata) {
		// we're not actually checking data. we're just checking if the port we're going to use is in use
		// and throwing an error if it is
		if (!available(9999)){
            warning = "NoPortClashValidator.warning";
			setMessage(idata.langpack.getString(warning));
			return Status.WARNING;
		} else {
			return Status.OK;
		}
	}

	private void setError(String string) {
		error = string;
	}
    private void setMessage(String string) {
        message = string;
    }

	public String getErrorMessageId() {
		// TODO Auto-generated method stub
		return error;
	}

	public String getWarningMessageId() {
		// TODO Auto-generated method stub
		return warning;
	}

	public boolean getDefaultAnswer() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public String getFormattedMessage() {
        return message;
    }

    public boolean available(int port) {
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
        Socket s = null;
        try {
            s = new Socket("localhost",port);
            // this creation throws Exception when the port is not in use; if it's in use, you'll get some response
            // return false if there's no Exception thrown (the port is in use.)
            return false;
        } catch (IOException e){} // desired result.
		try {
		    ss = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
           // ss = new ServerSocket(port);
			ss.setReuseAddress(true);
          //  ss.bind(new InetSocketAddress(port));
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
            // all checks succeeded
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
	}
}

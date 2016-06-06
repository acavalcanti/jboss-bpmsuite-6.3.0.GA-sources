package com.redhat.installer.ports.validator;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import sun.net.util.IPAddressUtil;

public class AddressValidator implements Validator
{
    private DomainValidator domainValidator = DomainValidator.getInstance();
    private InetAddressValidator ipv4Validator = InetAddressValidator.getInstance();

    public boolean validate(ProcessingClient client)
    {
        int numfields = client.getNumFields();

        // Do not check if field(0) is empty 
        // field(0) will be occupied by the System Property, which is optional
        // Alert: the limitation above cripples the usefulness of this validator.
        if (numfields > 1) {
            for (int i = 1; i < numfields; i++)
            {
                String value = client.getFieldContents(i);
                // not an ipv6 address
                if (!value.contains(":")){
                    return (isValidIPv4(value) || isValidDomain(value));
                }
                // contains colons; it's an ipv6 address
                else {
                    return isValidIPv6(value);
                }
            }
        }
        return true;
    }

    private boolean isValidIPv4(String ipv4){
        return ipv4Validator.isValid(ipv4);
    }

    private boolean isValidIPv6(String ipv6){
        /* Special cases:
         * ::ffff:aaa.bbb.ccc.ddd : we accept this, because it's the canonical ipv4 address
         * ::aaa.bbb.ccc.ddd      : we reject this, because it is deprecated. 
         * ::ffff%%eth0           : we reject the string if it has more than 1 '%'
         */
        if (ipv6.startsWith("::ffff:") && ipv6.split("\\.").length > 1){
            return isValidIPv4(ipv6.substring(7)) && IPAddressUtil.isIPv6LiteralAddress(ipv6);
        }
        else if (ipv6.split("\\.").length > 1){
            return false; // short circuit
        }

        else if (ipv6.indexOf("%", ipv6.indexOf("%")+1) != -1){
    	    return false;
        }
        
        return IPAddressUtil.isIPv6LiteralAddress(ipv6);
    }

    private boolean isValidDomain(String domain){
	// special exception for localhost
	if (domain.equals("localhost")){
	   return true;
	}
	else{
           return domainValidator.isValid(domain);
	}
    }
}

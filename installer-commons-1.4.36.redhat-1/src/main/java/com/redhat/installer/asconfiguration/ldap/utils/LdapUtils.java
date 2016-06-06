package com.redhat.installer.asconfiguration.ldap.utils;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * A collection of useful methods and constants for interacting with LDAP servers
 * Created by thauser on 9/29/14.
 */
public class LdapUtils {
    /**
     * Returns a DirContext object that uses the given searchDn and dnPassword to authenticate
     *
     * @param dnServer the location of the running server
     * @param searchDn the DN that the user with connection privileges resides
     * @param dnPassword the password for the user specified by searchDn
     * @return
     */
    public static DirContext makeConnection(String dnServer, String searchDn, String dnPassword){
        /** Ldap Explanation
         * 1. Indicate you're using the LDAP service provider
         * 2. Specify where ldap is running
         * 3. Specify name of the user/program that is doing the authentication
         * 4. Specify the credentials of the user/program doing the authentication.
         *
         * Note:
         * You can specify SECURITY_AUTHENTICATION with "none" or "simple"
         * If unspecified behaviour is determined by the service provider
         */
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, dnServer);
        env.put(Context.SECURITY_PRINCIPAL, searchDn);
        env.put(Context.SECURITY_CREDENTIALS, dnPassword);

        try
        {
            DirContext ctx = new InitialDirContext(env);
            return ctx;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Checks the user-given search for validity by searching the LDAP tree. Uses the filter as the user has entered,
     * and makes use of recursion based searches if the user has indicated this should be done.
     *
     * @param ctx
     * @param baseDn
     * @param filter
     * @return
     */
    public static boolean validateBaseDn(DirContext ctx, String baseDn, String filter, boolean recursive)
    {
        try {
            SearchControls ctls = new SearchControls();
            // set the search scope based upon recursion selected or not:
            if (recursive)
                ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            // try to find any entries matching the filter + the baseDN of the search:
            /*
            Three possibilities:
            1) Search is valid and has results -> return true
            2) Search is valid and has EMPTY results -> return false
            3) Search throws a NamingException, which means that the baseDN doesn't exist at all within the LDAP structure.
             */
            NamingEnumeration<SearchResult> search = ctx.search(baseDn, filter, ctls);

            // if there are results, we assume that a login of some kind will work
            if (search.hasMore()) {
                return true;
            } else {
                // nothing was returned. the user likely specified an incorrect filter
                return false;
            }
        } catch (NamingException ne){
            return false;
        }
    }
}


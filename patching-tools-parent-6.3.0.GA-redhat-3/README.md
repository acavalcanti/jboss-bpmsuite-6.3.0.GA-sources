JBoss BRMS and BPM Suite Patching Tools
=======================================
This repository contains few different modules used to create, bundle and also apply (at client side) the patches for
JBoss BRMS and BPM Suite 6.1.

See [BxMS 6.1 Patching Summary](https://mojo.redhat.com/docs/DOC-1021353) for more info about requirements, naming conventions
and overall summary of patching for BRMS and BPM Suite 6.1

Modules
-------
 * `client-patcher`: client side patching tool. Written in pure Java, so users only need Java installation to apply the patches.
 * `distribution-diffs`: responsible for generating diffs between specified versions of all the supported distributions
 (EAP, Generic, WAS and WLS wars, engines, supplementary tools, etc)
 * `patch-distributions`: bundles the the client patch tool together with the distribution diffs. These distributions are the final
 result that will be distributed to users/customers. Different distributions are created for BRMS and BPM Suite.
 * `integration-tests`: contains black-box integration (system) tests

The entire patch creation process consists of the following steps:

 1. Compile, test and package the client patcher (module `client-patcher`). It is later bundled into the the final
 patch zips.
 2. Generate the diffs between two specified versions, the minor version (e.g. 6.1.0) and the latest patch version (e.g. 6.1.3).
 3. Create the final -patch.zip files. These include the generated diffs + the client-patcher which is able to apply
 those diffs on top of user installations.

Creating the patch (diffs)
--------------------------
There are two kinds of BRMS/BPM Suite patches: the first one in the stream and the others (second, third, ...). The difference is that
the first patch does not depend on any metadata from previous patches (there are none). So the build process is a bit
different. To generate first patch one needs to use profile called `first-patch`, e.g. `mvn clean install -Pfirst-patch`.
This way no dependency on previous patches is defined.

To generate second, third, etc patch the command is simply `mvn clean install`. The subsequent patches need artifacts (metadata)
built as part of the previous patch release, so these artifact need to available either in local repository
or in some remote one.

The diffs are created as a part of the `distribution-diffs` module execution. Maven is used to download and unzip the
distribution files. After that simple Bash script is called which will created all the diffs (+ some additional metadata).
These diff are then packaged into zip files and exposed as Maven build artifacts.

The diff itself includes any file that changed (read has different checksum) between the two specified zip files
(the deliverables are zips with war files inside). For example, as part of the BRMS 6.1.0 release there is file called
`jboss-brms-6.1.0.GA-deployable-eap6.x.zip`. As part of the BRMS 6.1.1 release there is file called
`jboss-brms-6.1.1.GA-deployable-eap6.x.zip`. The script takes two directories (with the unzipped content),
compares them and creates a directory with the new content (updated or added files) and some metadata files. As a result, there
are these "dumb" diffs (basically anything with different checksum) as basically any .jar file that has been rebuilt will
be included. Advantage of these "dumb" diffs is that we don't need to update any possibly references to jars names. The
distribution looks exactly the same after applying the diff as it was originally built. Disadvantage is that the patch
zips are slightly bigger.


Applying the patch
------------------
The patch (set of updates) is distributed as a zip file with simple .sh and .bat scripts (inside the zip) that offer easy and
automatic way to apply the updates to current installations.

The script takes two mandatory parameters: `<path-to-distribution-root>` and `<distribution-type>`.
For example `./apply-updates.sh /opt/jboss-eap-6.4 eap6.x` will apply the updates to specified EAP bundle. Please note that
only updates for BRMS/BPM Suite are part of the patch. Patches to EAP itself need to be applied using the EAP patching
mechanism.

See output of `apply-updates.[sh|bat] -h` for more information about how to apply the updates to specific distribution types.

As a part of JBoss BRMS and JBoss BPM Suite patch release, following deliverables are created:

 * zip for BRMS customers (`jboss-brms-<version>-patch.zip`)
 * zip for BPM Suite customers (`jboss-bpmsuite-<version>-patch.zip`)

Note that there is one additional zip with Maven repository updates (`jboss-brms-bpmsuite-<version>-incremental-maven-repository.zip`).
This one is, however, created together with the Maven repository zip itself and _not_ using the patch tooling in this source
repository.

Handling cumulativity and content from one-off patches
------------------------------------------
The generated patch zips are cumulative. That means that the latest patch can be applied to any patch version before that,
down to the base minor version. For example, the 6.1.3 patch zip can be applied on top of 6.1.0, 6.1.1 and 6.1.2. To accomplish
this the client patcher tool needs some information about all the previous patches, namely list of the files that were updated/added.
This way the tool makes sure all these previously added files are removed before applying the changes (the -patch.zip file
contains only the updated/new files that should be applied on top of the specified minor version, so the distributions
need to be cleaned-up into some known state). This great decreases the size of the patches as it is not needed to include
multiple diffs (e.g. diff between 6.1.0 and 6.1.1 and diff between 6.1.1 and 6.1.2). Disadvantage is that the patches
can not be rolled back easily.

When applying the updates, the client-patcher needs to remove any files possibly added by one-off patches. These include
.jar files in most cases and bigger parts of the war files in case of fixes for GWT-based UI. Unfortunately, the distributions
do not contain any metadata about what one-off patches were applied. The tool just follows few simple rules to remove
anything that was potentially added by the one-off patches.

Additional features
-------------------
 * **Backup** - before applying any updates, the client script will backup the specified distribution. It simply copies the
   distribution file/directory into the `backup/<current-timestamp>` subdirectory. The top-level `backup` directory is created
   at the same filesystem level as the `apply-updates` script itself.

 * **Blacklist** - blacklist provides a way to inform the script about files that should not be updated when applying the updates.
   These are usually configuration files. See the `blacklist.txt` file inside the patch distribution for list of files
   blacklisted by default. If there is an update of one of the blacklisted files, this new file will not replace the old one
   and rather new file with `.new` suffix will be created. It is then responsibility of the user to look into the differences and
   apply them manually if needed. In case there is a file that should be removed, but it is also on the blacklist, the file will
   not be removed and empty marker file with `.removed` suffix will be created. User then needs to investigate and remove the
   original file if it is no longer needed.

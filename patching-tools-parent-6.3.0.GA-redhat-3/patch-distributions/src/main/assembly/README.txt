This patch can be applied using the apply-updates.[sh|bat] script. Linux/Unix users need to use apply-updates.sh,
Windows users apply-updates.bat. Script takes two parameters: path to distribution root that should
be patched and type of the distribution. You can run apply-updates.[sh|bat] -h to get more information about how
to apply the patch and which distributions are supported.

There is a blacklist.txt file in the root of the patch directory. You can edit the file and add paths to files that
you don't want to update. Instead of overwriting those files, new files with ".new" extension are created. Similarly,
when a file is supposed to be removed by the patch and it is also in the blacklist.txt, it won't be deleted and instead
only marker file with ".removed" suffix will be created.

Further information:
https://access.redhat.com/articles/1455733
https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_BPM_Suite/6.2/html/Installation_Guide/chap-Patching_and_Upgrading_Red_Hat_JBoss_BPM_Suite.html
https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_BRMS/6.2/html/Installation_Guide/chap-Patching_and_Upgrading_Red_Hat_JBoss_BRMS.html

package com.izforge.izpack.panels;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.IzPanel;

import javax.swing.*;
import java.util.List;

/**
 * Created by aabulawi on 23/02/15.
 */
public class FileDirInputField extends FileInputField {


    public FileDirInputField(IzPanel parent, InstallData data, boolean directory, String set, int size, List<ValidatorContainer> validatorConfig, boolean mustexist) {
        super(parent, data, directory, set, size, validatorConfig);
        this.mustExist = mustexist;
    }

    @Override
    protected void prepareFileChooser(JFileChooser filechooser) {
        filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if ((fileExtension != null) && (fileExtensionDescription != null))
        {
            UserInputFileFilter fileFilter = new UserInputFileFilter();
            fileFilter.setFileExt(fileExtension);
            fileFilter.setFileExtDesc(fileExtensionDescription);
            filechooser.setFileFilter(fileFilter);
        }
    }
}

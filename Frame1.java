/** Copyright (C) 2001-2011 by Johan Kotlinski

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE. */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;

public class Frame1 extends JFrame {
    String versionString = "LSD-Patcher v.12";
    JPanel contentPane;
    JPanel jPanel1 = new JPanel();
    TitledBorder titledBorder1;
    //JLabel fileNameLabel = new JLabel();
    JComboBox bankBox = new JComboBox();
    JList instrList = new JList();

    RandomAccessFile romFile;
    int totSampleSize=0;
    int currentSample[]=new int[getBankCount()];

    byte romImage[]=new byte[0x4000 * getBankCount()];

    boolean bankIsEditable[]=new boolean[getBankCount()];
    FakeFile instrFile[]= new FakeFile[getBankCount()];

    JMenuItem saveROMItem;
    JMenuItem importKitsItem;
    String latestRomKitPath="";
    String latestWavPath="";
    JButton loadKitButton = new JButton();
    JButton exportKitButton = new JButton();
    JButton createKitButton = new JButton();
    TitledBorder titledBorder2;
    JButton renameKitButton = new JButton();
    JTextArea kitTextArea = new JTextArea();
    JButton addSampleButton = new JButton();
    JButton dropSampleButton = new JButton();
    JButton saveROMButton = new JButton();
    JLabel kitSizeLabel = new JLabel();
    JPanel jPanel2 = new JPanel();
    TitledBorder titledBorder3;
    JSlider ditherSlider = new JSlider();
    GridLayout gridLayout1 = new GridLayout();

    JMenuBar menuBar = new JMenuBar();

    /**Construct the frame*/

    public Frame1() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        String listData[]={"1.","2.","3.","4.","5.","6.","7.","8.","9.","10.","11.",
            "12.","13.","14.","15."};
        for(int i=0;i<15;i++) {
            currentSample[i]=0;
            bankIsEditable[i]=false;
        }
        instrList.setListData(listData);

        if (!selectRomToLoad()) {
            System.exit(0);
        }
    }

    private void buildMenus()
    {
        //menu stuff
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Open ROM...", KeyEvent.VK_O);
        menuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectRomToLoad();
                }});
        menu.add(menuItem);

        menu.addSeparator();

        importKitsItem = new JMenuItem("Import kits from ROM...", KeyEvent.VK_I);
        importKitsItem.setEnabled(false);
        importKitsItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                importKits_actionPerformed(e);
                }
                });

        menu.add(importKitsItem);
        menu.addSeparator();

        saveROMItem = new JMenuItem("Save ROM...", KeyEvent.VK_S);
        saveROMItem.setEnabled(false);
        saveROMItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                saveROMButton_actionPerformed();
                }});
        menu.add(saveROMItem);

        //help menu
        /*
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        menuItem = new JMenuItem("Documentation", KeyEvent.VK_D);
        menuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                openDoc_actionPerformed(e);
                }
                });
        menu.add(menuItem);
        */

        setJMenuBar(menuBar);
    }

    class DitherListener implements javax.swing.event.ChangeListener {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            compileKit();
        }
    }

    /**Component initialization*/
    private void jbInit() throws Exception  {
        //setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[Your Icon]")));
        contentPane = (JPanel) this.getContentPane();
        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(164, 164, 159)),"ROM Image");
        titledBorder2 = new TitledBorder(BorderFactory.createLineBorder(new Color(153, 153, 153),2),"rename kit");
        titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(164, 164, 159)),"Dithering (0-16)");
        contentPane.setLayout(null);
        this.setSize(new Dimension(400, 414));
        this.setResizable(false);
        this.setTitle("LSDPatcher");

        new FileDrop(contentPane, new FileDrop.Listener() {
            public void filesDropped(java.io.File[] files) {
                for (java.io.File file : files) {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".wav")) {
                        if (!createKitButton.isEnabled()) {
                            JOptionPane.showMessageDialog(contentPane,
                                "Open .gb file before adding samples.",
                                "Can't add sample!",
                                JOptionPane.ERROR_MESSAGE);
                            continue;
                        }
                        if (!addSampleButton.isEnabled()) {
                            createKitButton_actionPerformed();
                        }
                        addSample(file);
                    } else if (fileName.endsWith(".gb")) {
                        loadRom(file);
                    } else if (fileName.endsWith(".kit")) {
                        if (!createKitButton.isEnabled()) {
                            JOptionPane.showMessageDialog(contentPane,
                                "Open .gb file before adding samples.",
                                "Can't add sample!",
                                JOptionPane.ERROR_MESSAGE);
                            continue;
                        }
                        loadKit(file);
                    } else {
                        JOptionPane.showMessageDialog(contentPane,
                            "Unknown file type!",
                            "File error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }});

        jPanel1.setBorder(titledBorder1);
        jPanel1.setBounds(new Rectangle(8, 6, 193, 375-17));
        jPanel1.setLayout(null);
        //fileNameLabel.setText("No file opened!");
        //fileNameLabel.setBounds(new Rectangle(9, 18, 174, 17));
        bankBox.setBounds(new Rectangle(8, 18, 176, 25));
        bankBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bankBox_actionPerformed(e);
                }});
        instrList.setBorder(BorderFactory.createEtchedBorder());
        instrList.setBounds(new Rectangle(8, 64-17, 176, 280));
        instrList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = instrList.locationToIndex(e.getPoint());
                playSample(index);
            }});

        contentPane.setMinimumSize(new Dimension(1, 1));
        contentPane.setPreferredSize(new Dimension(400, 414));
        loadKitButton.setEnabled(false);
        loadKitButton.setText("load kit");
        loadKitButton.setBounds(new Rectangle(212, 78-72, 170, 28));
        loadKitButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                loadKitButton_actionPerformed();
                }});
        exportKitButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                exportKitButton_actionPerformed();
                }});
        exportKitButton.setBounds(new Rectangle(212, 110-72, 170, 28));
        exportKitButton.setEnabled(false);
        exportKitButton.setText("export kit");
        createKitButton.setEnabled(false);
        createKitButton.setText("create new kit");
        createKitButton.setBounds(new Rectangle(212, 184-72, 170, 28));
        createKitButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                createKitButton_actionPerformed();
                }
                });
        renameKitButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                renameKitButton_actionPerformed(e);
                }
                });
        renameKitButton.setEnabled(false);
        renameKitButton.setText("rename kit");
        renameKitButton.setBounds(new Rectangle(287, 143-72, 95, 27));
        kitTextArea.setBorder(BorderFactory.createEtchedBorder());
        kitTextArea.setBounds(new Rectangle(212, 146-72, 67, 21));
        addSampleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectSampleToAdd();
                }});
        addSampleButton.setBounds(new Rectangle(212, 216-72, 170, 28));
        addSampleButton.setEnabled(false);
        addSampleButton.setText("add sample");
        dropSampleButton.setText("drop sample");
        dropSampleButton.setEnabled(false);
        dropSampleButton.setBounds(new Rectangle(212, 248-72, 170, 28));
        dropSampleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                dropSampleButton_actionPerformed(e);
                }
                });
        saveROMButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                saveROMButton_actionPerformed();
                }});
        saveROMButton.setBounds(new Rectangle(212, 280-72, 170, 28));
        saveROMButton.setEnabled(true);
        saveROMButton.setText("save rom...");
        kitSizeLabel.setToolTipText("");
        kitSizeLabel.setText("0/3fa0 bytes used");
        kitSizeLabel.setBounds(new Rectangle(8, 346-17, 169, 22));
        jPanel2.setBorder(titledBorder3);
        jPanel2.setBounds(new Rectangle(210, 315-72, 174, 66));
        jPanel2.setLayout(gridLayout1);
        ditherSlider.setMajorTickSpacing(4);
        ditherSlider.setMaximum(16);
        ditherSlider.setMinorTickSpacing(1);
        ditherSlider.setPaintLabels(true);
        ditherSlider.setPaintTicks(true);
        ditherSlider.setToolTipText("");
        ditherSlider.setValue(0);
        ditherSlider.setEnabled(false);
        ditherSlider.addChangeListener(new DitherListener());
        contentPane.add(jPanel1, null);
        //jPanel1.add(fileNameLabel, null);
        jPanel1.add(bankBox, null);
        jPanel1.add(instrList, null);
        jPanel1.add(kitSizeLabel, null);
        contentPane.add(loadKitButton, null);
        contentPane.add(exportKitButton, null);
        contentPane.add(renameKitButton, null);
        contentPane.add(kitTextArea, null);
        contentPane.add(createKitButton, null);
        contentPane.add(addSampleButton, null);
        contentPane.add(dropSampleButton, null);
        contentPane.add(saveROMButton, null);
        contentPane.add(jPanel2, null);
        JLabel versionLabel = new JLabel(versionString, SwingConstants.RIGHT);
        versionLabel.setBounds(new Rectangle(210, 315, 174, 66));
        contentPane.add(versionLabel);
        jPanel2.add(ditherSlider, null);

        buildMenus();
    }
    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
        }
    }

    void playSample(int index) {
        int offset = getSelectedROMBank() * 0x4000 + index * 2;
        int start = (0xff & romImage[offset]) | ((0xff & romImage[offset + 1]) << 8);
        int stop = (0xff & romImage[offset + 2]) | ((0xff & romImage[offset + 3]) << 8);
        if (stop <= start) {
            return;
        }
        byte[] arr = new byte[stop - start];
        for (int i = start; i < stop; ++i) {
            arr[i - start] = romImage[getSelectedROMBank() * 0x4000 - 0x4000 + i];
        }
        try {
            Sound.play(arr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Audio error",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void loadRom(File gbFile) {
        try {
            latestRomKitPath = gbFile.getAbsoluteFile().toString();
            setTitle(latestRomKitPath);
            romFile = new RandomAccessFile(gbFile, "r");
            romFile.readFully(romImage);
            romFile.close();
            saveROMItem.setEnabled(true);
            importKitsItem.setEnabled(true);
            loadKitButton.setEnabled(true);
            exportKitButton.setEnabled(true);
            renameKitButton.setEnabled(true);
            createKitButton.setEnabled(true);
            flushWavFiles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
        updateRomView();
    }

    // Returns true on success.
    boolean selectRomToLoad() {
        JFileChooser chooser = new JFileChooser(latestRomKitPath);
        GBFileFilter filter = new GBFileFilter();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Load ROM image");
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            loadRom(chooser.getSelectedFile());
            return true;
        }
        return false;
    }

    private boolean isKitBank ( int a_bank ) {
        int l_offset = ( a_bank ) * 0x4000;
        byte l_char_1 = romImage[l_offset++];
        byte l_char_2 = romImage[l_offset];
        return ( l_char_1 == 0x60 && l_char_2 == 0x40 );
    }

    private boolean isEmptyKitBank ( int a_bank ) {
        int l_offset = ( a_bank ) * 0x4000;
        byte l_char_1 = romImage[l_offset++];
        byte l_char_2 = romImage[l_offset];
        return ( l_char_1 == -1 && l_char_2 == -1 );
    }

    private String getKitName ( int a_bank ) {
        if ( isEmptyKitBank ( a_bank ) ) {
            return "Empty";
        }

        byte buf[]=new byte[6];
        int offset=(a_bank)*0x4000+0x52;
        for(int i=0;i<6;i++) {
            buf[i]=romImage[offset++];
        }
        return new String ( buf );
    }

    private int getBankCount() {
        return 64;
    }

    private void updateRomView() {
        int tmp=bankBox.getSelectedIndex();
        bankBox.removeAllItems();

        //do banks
        int l_ui_index = 0;
        for(int bankNo=0; bankNo < getBankCount(); bankNo++) {
            if ( isKitBank ( bankNo ) || isEmptyKitBank ( bankNo ) ) {
                bankBox.addItem( ++l_ui_index + ". "+ getKitName ( bankNo ) );
            }
        }
        bankBox.setSelectedIndex(tmp==-1?0:tmp);
        updateBankView();

    }

    int m_selected = -1;
    private int getSelectedUiBank()
    {
        if ( bankBox.getSelectedIndex() > -1 )
        {
            m_selected = bankBox.getSelectedIndex();
        }
        return m_selected;
    }

    private int getSelectedROMBank ( )
    {
        int l_rom_bank = 0;
        int l_ui_bank = 0;

        for ( ;; )
        {
            if ( isKitBank( l_rom_bank ) || isEmptyKitBank( l_rom_bank ) )
            {
                if ( getSelectedUiBank() == l_ui_bank )
                {
                    return l_rom_bank;
                }
                l_ui_bank++;
            }
            l_rom_bank++;
        }

    }

    private int getROMOffsetForSelectedBank ( )
    {
        return getSelectedROMBank() * 0x4000;
    }

    private void updateBankView() {
        byte buf[]=new byte[3];
        String s[]=new String[15];

        totSampleSize=0;

        int bankOffset = getROMOffsetForSelectedBank();
        instrList.removeAll();
        //do banks

        //update names
        int offset=bankOffset+0x22;
        for(int instrNo=1;instrNo<16;instrNo++) {
            boolean isNull=false;
            for(int i=0;i<3;i++) {
                buf[i]=romImage[offset++];
                if(isNull) {
                    buf[i]='-';
                } else {
                    if(buf[i]==0) {
                        buf[i]='-';
                        isNull=true;
                    }
                }
            }
            s[instrNo-1]=instrNo+". "+new String(buf);
            FakeFile f = instrFile[instrNo-1];
            if(bankIsEditable[ getSelectedUiBank() ] && f!=null) {
                int flen=(int)(f.length()/2-f.length()/2%0x10);
                totSampleSize+=flen;
                s[instrNo-1]+=" ("+f.getName()+", "+
                    Integer.toHexString(flen)+")";
            }
        }
        instrList.setListData(s);

        updateKitSizeLabel();
        addSampleButton.setEnabled(inBankEditMode());
        dropSampleButton.setEnabled(inBankEditMode());
        ditherSlider.setEnabled(inBankEditMode());
    }

    boolean inBankEditMode() {
        return bankIsEditable[getSelectedUiBank()];
    }

    void updateKitSizeLabel() {
        int sampleSize = 0;
        if (inBankEditMode()) {
            sampleSize = totSampleSize;
        } else if (isKitBank(getSelectedROMBank())) {
            int offset = getROMOffsetForSelectedBank();
            int max = 0;
            for (int sample = 0; sample < 0x10; ++sample) {
                int pos = (0xff & romImage[offset]) | ((0xff & romImage[offset + 1]) << 8);
                max = pos > max ? pos : max;
                offset += 2;
            }
            sampleSize = max - 0x4060;
        }
        kitSizeLabel.setText(Integer.toHexString(sampleSize) + "/3fa0 bytes used");
        boolean tooFull = sampleSize > 0x3fa0;

        Color c = tooFull ? Color.red : Color.black;
        kitSizeLabel.setForeground(c);
        instrList.setForeground(c);
    }

    void bankBox_actionPerformed(ActionEvent e) {
        updateBankView();
    }

    void importKits ( File f ) {
        try {
            int inBank = 0;
            int outBank = 0;
            int copiedBankCount = 0;
            FileInputStream in = new FileInputStream ( f.getAbsolutePath() );
            while ( in.available() > 0 ) 
            {
                byte[] inBuf = new byte[0x4000];
                in.read ( inBuf );
                if ( inBuf[0] == 0x60 && inBuf[1] == 0x40 )
                {
                    //is kit bank
                    outBank++;
                    while ( !isKitBank(outBank) && !isEmptyKitBank(outBank ) )
                    {
                        outBank++;
                    }
                    int outPtr = outBank * 0x4000;
                    for ( int i = 0; i < 0x4000; i++ )
                    {
                        romImage[outPtr++] = inBuf[i];  
                    }
                    copiedBankCount++;
                }
                ++inBank;
            }
            updateRomView();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void importKits_actionPerformed(ActionEvent e) {
        JFileChooser chooser=new JFileChooser(latestRomKitPath);
        GBFileFilter filter = new GBFileFilter();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("select rom to import kits from");
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f=chooser.getSelectedFile();
            latestRomKitPath=f.getAbsolutePath().toString();

            if(!f.toString().toUpperCase().endsWith(".GB")) {
                f=new File(f.getAbsoluteFile().toString()+".gb");
            }
            //fileNameLabel.setText(f.getAbsoluteFile().toString());

            importKits( f );
        }
    }

    void saveROMButton_actionPerformed() {
        JFileChooser chooser=new JFileChooser(latestRomKitPath);
        GBFileFilter filter = new GBFileFilter();
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("save rom image");
        int returnVal = chooser.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File f=chooser.getSelectedFile();
                latestRomKitPath=f.getAbsolutePath().toString();

                if(!f.toString().toUpperCase().endsWith(".GB")) {
                    f=new File(f.getAbsoluteFile().toString()+".gb");
                }
                //fileNameLabel.setText(f.getAbsoluteFile().toString());

                romFile=new RandomAccessFile(f,"rw");
                romFile.write(romImage);
                romFile.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void exportKitButton_actionPerformed() {
        JFileChooser chooser=new JFileChooser(latestRomKitPath);
        chooser.setFileFilter(new KitFileFilter());
        chooser.setDialogTitle("export kit");
        int returnVal = chooser.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File f=chooser.getSelectedFile();

                if(!f.toString().toUpperCase().endsWith(".KIT")) {
                    f=new File(chooser.getSelectedFile().getAbsoluteFile().toString()+".kit");
                }

                byte buf[]=new byte[0x4000];
                int offset=getROMOffsetForSelectedBank();
                RandomAccessFile bankFile=new RandomAccessFile(f,"rw");

                for(int i=0;i<buf.length;i++) {
                    buf[i]=romImage[offset++];
                }
                bankFile.write(buf);
                bankFile.close();
                latestRomKitPath=f.getAbsolutePath().toString();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                        JOptionPane.ERROR_MESSAGE);
            }
            updateRomView();
        }
    }

    void loadKit(File kitFile) {
        try {
            byte buf[]=new byte[0x4000];
            int offset=getROMOffsetForSelectedBank();
            RandomAccessFile bankFile = new RandomAccessFile(kitFile, "r");
            bankFile.readFully(buf);

            for(int i=0;i<buf.length;i++) {
                romImage[offset++]=buf[i];
            }
            bankFile.close();
            latestRomKitPath = kitFile.getAbsolutePath().toString();
            flushWavFiles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
        updateRomView();
    }

    void loadKitButton_actionPerformed() {
        JFileChooser chooser=new JFileChooser(latestRomKitPath);
        chooser.setFileFilter(new KitFileFilter());
        chooser.setDialogTitle("load kit");
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            loadKit(chooser.getSelectedFile());
        }
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    private String dropExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext=s.substring(0,i);
        }
        return ext;
    }

    void createKitButton_actionPerformed() {
        currentSample[ getSelectedUiBank() ]=0;

        bankIsEditable[ getSelectedUiBank() ]=true;

        //clear all bank
        int offset = getROMOffsetForSelectedBank() + 2;
        int max_offset = getROMOffsetForSelectedBank() + 0x4000; 
        while ( offset < max_offset )
        {
            romImage[offset++] = 0;
        }

        //clear kit name
        offset=getROMOffsetForSelectedBank() + 0x52;
        for(int i=0;i<6;i++) {
            romImage[offset++]=' ';
        }

        //clear instrument names
        offset = getROMOffsetForSelectedBank() + 0x22;
        byte b[]=new byte[3];
        for(int i=0;i<15;i++) {
            romImage[offset++]=0;
            romImage[offset++]='-';
            romImage[offset++]='-';
        }

        flushWavFiles();

        updateRomView();
    }

    void flushWavFiles() {
        instrFile = new FakeFile[getBankCount()];
    }

    void renameKitButton_actionPerformed(ActionEvent e) {
        int offset= getROMOffsetForSelectedBank() +0x52;
        String s=kitTextArea.getText().toUpperCase();
        for(int i=0;i<6;i++) {
            if(i<s.length()) {
                romImage[offset++]=(byte)s.charAt(i);
            } else {
                romImage[offset++]=' ';
            }
        }
        compileKit();
        updateRomView();
    }

    void addSample(File wavFile) {
        int offset = getROMOffsetForSelectedBank() + 0x22 +
            currentSample[getSelectedUiBank()] * 3;
        String s = dropExtension(wavFile).toUpperCase();
        if(s.length() > 0) {
            romImage[offset++]=(byte)s.charAt(0);
        } else {
            romImage[offset++]='-';
        }
        if(s.length() > 1) {
            romImage[offset++]=(byte)s.charAt(1);
        } else {
            romImage[offset++]='-';
        }
        if(s.length() > 2) {
            romImage[offset++]=(byte)s.charAt(2);
        } else {
            romImage[offset++]='-';
        }

        FakeFile file = convertWav(wavFile);
        if (file == null) {
            return;
        }
        instrFile[currentSample[getSelectedUiBank()]] = file;

        currentSample[getSelectedUiBank()]++;

        latestWavPath = wavFile.getAbsolutePath().toString();

        compileKit();
        updateRomView();
    }

    void selectSampleToAdd() {
        JFileChooser chooser=new JFileChooser(latestWavPath);
        chooser.setFileFilter(new RawFileFilter());
        chooser.setDialogTitle("load sample");
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            addSample(chooser.getSelectedFile());
        }
    }

    private static long readWord(FileInputStream in) throws IOException
    {
        long ret = 0;
        byte[] word = new byte[4];
        in.read ( word );

        ret += word[0];
        ret <<= 8;

        ret += word[1];
        ret <<= 8;

        ret += word[2];
        ret <<= 8;

        ret += word[3];

        return ret;
    }

    private static int readEndianShort(FileInputStream in) throws IOException
    {
        int ret = 0;
        byte[] word = new byte[2];
        in.read ( word );

        ret += val(word[1]);
        ret <<= 8;
        ret += val(word[0]);

        return ret;
    }

    static private int val(byte b)
    {
        if ( b >= 0 )
        {
            return b;
        }
        return 0x100 + b;
    }

    private static long readEndianWord(FileInputStream in) throws IOException
    {
        long ret = 0;
        byte[] word = new byte[4];
        in.read ( word );

        ret += val(word[3]);
        ret <<= 8;

        ret += val(word[2]);
        ret <<= 8;

        ret += val(word[1]);
        ret <<= 8;

        ret += val(word[0]);

        return ret;
    }

    private FakeFile convertWav(File file)
    {
        int ch = 0;
        long sampleRate = 0;
        int blockAlign = 0;
        int bits = 0;

        try {
            FileInputStream in = new FileInputStream( file.getAbsolutePath() );

            long riffId = readWord(in);
            //assert riffId == 0x52494646; //'RIFF'
            readWord(in); //skip file size

            long waveId = readWord(in);
            //assert waveId == 0x57415645; //'WAVE'

            while ( in.available() != 0 )
            {
                long chunkId = readWord(in);
                long chunkSize = readEndianWord(in);

                if ( chunkId == 0x666D7420 ) // fmt
                {
                    int compression = readEndianShort(in);
                    if (compression != 1) {
                        JOptionPane.showMessageDialog(this,
                                "Sample is compressed. Only PCM .wav files are supported.",
                                "Format error",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    ch = readEndianShort(in);
                    if (ch > 2) {
                        JOptionPane.showMessageDialog(this,
                                "Unsupported number of channels!",
                                "Format error",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    sampleRate = readEndianWord(in);
                    readWord(in); //avg. bytes/second
                    blockAlign = readEndianShort(in);
                    bits = readEndianShort(in);
                    if (bits != 16) {
                        JOptionPane.showMessageDialog(this,
                                "Only 16-bit .wav is supported!",
                                "Format error",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                    assert blockAlign == 2;
                }
                else if ( chunkId == 0x64617461 ) // data
                {
                    byte[] buf = new byte[(int)chunkSize];
                    in.read(buf);

                    //convert to mono
                    assert blockAlign == 2;
                    if ( ch == 2 )
                    {
                        int inIt = 0;
                        int outIt = 0;
                        while ( inIt < chunkSize )
                        {
                            buf[outIt++] = buf[inIt++];
                            buf[outIt++] = buf[inIt++];
                            inIt += 2;
                        }
                        chunkSize /= 2;
                        ch = 1;
                    }

                    //convert to 8-bit
                    {
                        int inIt = 1;
                        int outIt = 0;

                        byte max = 0;
                        byte min = 0;

                        while ( inIt < chunkSize )
                        {
                            byte val = buf[inIt];
                            //System.out.print(val+" ");
                            if ( val < min ) min = val;
                            if ( val > max ) max = val;
                            buf[outIt] = val;
                            outIt++;
                            inIt+=2;
                        }
                        chunkSize /= 2;
                        blockAlign = 1;
                    }

                    int frames = (int)chunkSize;

                    int outFreq = 11468;
                    int outFrames = ( outFreq * frames ) / (int)sampleRate;

                    double readPos = 0.0;
                    double advance = (double)sampleRate / (double)outFreq;

                    byte[] outBuf = new byte[outFrames];
                    int writePos = 0;

                    while ( writePos < outFrames )
                    {
                        byte val = buf[(int)readPos];
                        outBuf[writePos++] = val;
                        readPos += advance;
                    }

                    return new FakeFile ( outBuf, file.getName() );
                }
                else
                {
                    in.skip(chunkSize);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    void compileKit() {
        updateBankView();
        if(totSampleSize>0x3fa0) {
            kitSizeLabel.setText(Integer.toHexString(totSampleSize)+"/3fa0 bytes used");
            return;
        }
        kitSizeLabel.setText(Integer.toHexString(totSampleSize)+" bytes written");
        sbc.DITHER_VAL=ditherSlider.getValue();

        byte newSamples[]=new byte[0x4000];
        int lengths[]=new int[15];
        sbc.handle(newSamples,instrFile,lengths);

        // Checks if at least one sample has been added.
        boolean hasAnySample = false;
        for (int i = 0; i < 15; ++i) {
            if (lengths[i] > 0) {
                hasAnySample = true;
                break;
            }
        }
        if (!hasAnySample) {
            // Not allowed to create kits without samples!
            return;
        }

        //copy sampledata to ROM image
        int offset = getROMOffsetForSelectedBank() + 0x60;
        int offset2=0x60;
        do {
            romImage[offset++]=newSamples[offset2++];
        } while(offset2!=0x4000);

        //update samplelength info in rom image
        int bankOffset=0x4060;
        offset=getROMOffsetForSelectedBank();
        romImage[offset++]=0x60;
        romImage[offset++]=0x40;

        for(int i=0;i<15;i++) {
            bankOffset+=lengths[i];
            if(lengths[i]!=0) {
                romImage[offset++]=(byte)(bankOffset&0xff);
                romImage[offset++]=(byte)(bankOffset>>8);
            } else {
                romImage[offset++]=0;
                romImage[offset++]=0;
            }
        }
    }

    void dropSampleButton_actionPerformed(ActionEvent e) {
        int index=instrList.getSelectedIndex();

        if(index==-1) {
            return;
        }

        //move up instr file links
        for(int i=index;i<14;i++) {
            instrFile[i]=instrFile[i+1];
        }
        instrFile[14]=null;

        //move up instr names
        int offset= getROMOffsetForSelectedBank() +0x22+index*3;
        int i;
        for(i=offset;i< getROMOffsetForSelectedBank() +0x22+14*3;i+=3) {
            romImage[i]=romImage[i+3];
            romImage[i+1]=romImage[i+4];
            romImage[i+2]=romImage[i+5];
        }
        romImage[i]=0;
        romImage[i+1]='-';
        romImage[i+2]='-';
        if(index<currentSample[ getSelectedUiBank() ]) {
            currentSample[ getSelectedUiBank() ]--;
        }
        compileKit();
        updateBankView();
    }

}

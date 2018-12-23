/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL), 
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR 
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING, 
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER 
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE 
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES 
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES, 
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER 
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT 
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.ui;

import static org.broad.igv.ui.UIConstants.ABOUT_TOOLTIP;
import static org.broad.igv.ui.UIConstants.EXIT_TOOLTIP;
import static org.broad.igv.ui.UIConstants.HELP_TOOLTIP;
import static org.broad.igv.ui.UIConstants.IMPORT_GENOME_TOOLTIP;
import static org.broad.igv.ui.UIConstants.IMPORT_REGION_TOOLTIP;
import static org.broad.igv.ui.UIConstants.LOAD_GENOME_LIST_MENU_ITEM;
import static org.broad.igv.ui.UIConstants.LOAD_GENOME_TOOLTIP;
import static org.broad.igv.ui.UIConstants.PREFERENCE_TOOLTIP;
import static org.broad.igv.ui.UIConstants.SAVE_IMAGE_TOOLTIP;
import static org.broad.igv.ui.UIConstants.SELECT_DISPLAYABLE_ATTRIBUTES_TOOLTIP;
import static org.broad.igv.ui.UIConstants.SERVER_BASE_URL;
import static org.broad.igv.ui.UIConstants.SHOW_ATTRIBUTE_DISPLAY_TOOLTIP;
import static org.broad.igv.ui.UIConstants.SHOW_HEATMAP_LEGEND_TOOLTIP;
import static org.broad.igv.ui.UIConstants.TUTORIAL_TOOLTIP;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Event;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.basic.BasicBorders;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.gs.GSOpenSessionMenuAction;
import org.broad.igv.gs.GSSaveSessionMenuAction;
import org.broad.igv.gs.GSUtils;
import org.broad.igv.lists.GeneListManagerUI;
import org.broad.igv.lists.VariantListManager;
import org.broad.igv.tools.IgvToolsGui;
import org.broad.igv.track.AttributeManager;
import org.broad.igv.ui.action.ClearRegionsMenuAction;
import org.broad.igv.ui.action.DeletePanelsMenuAction;
import org.broad.igv.ui.action.ExportRegionsMenuAction;
import org.broad.igv.ui.action.ExportTrackNamesMenuAction;
import org.broad.igv.ui.action.FilterTracksMenuAction;
import org.broad.igv.ui.action.FitDataToWindowMenuAction;
import org.broad.igv.ui.action.GetSessionInfoMenuAction;
import org.broad.igv.ui.action.GetShowPropsMenuAction;
import org.broad.igv.ui.action.GetStatusTrackerInfoMenuAction;
import org.broad.igv.ui.action.GroupTracksMenuAction;
import org.broad.igv.ui.action.ImportRegionsMenuAction;
import org.broad.igv.ui.action.LinkToSessionMenuAction;
import org.broad.igv.ui.action.LoadFilesMenuAction;
import org.broad.igv.ui.action.LoadFromDatabaseAction;
import org.broad.igv.ui.action.LoadFromGSMenuAction;
import org.broad.igv.ui.action.LoadFromServerAction;
import org.broad.igv.ui.action.LoadFromURLMenuAction;
import org.broad.igv.ui.action.MenuAction;
import org.broad.igv.ui.action.NavigateRegionsMenuAction;
import org.broad.igv.ui.action.NewPanelMenuAction;
import org.broad.igv.ui.action.NewSessionMenuAction;
import org.broad.igv.ui.action.OpenSessionMenuAction;
import org.broad.igv.ui.action.RemoveUserDefinedGenomeMenuAction;
import org.broad.igv.ui.action.ResetPreferencesAction;
import org.broad.igv.ui.action.RunScriptMenuAction;
import org.broad.igv.ui.action.SaveSessionMenuAction;
import org.broad.igv.ui.action.SearchMenuAction;
import org.broad.igv.ui.action.SetTrackHeightMenuAction;
import org.broad.igv.ui.action.SortTracksMenuAction;
import org.broad.igv.ui.legend.LegendDialog;
import org.broad.igv.ui.panel.MainPanel;
import org.broad.igv.ui.panel.ReorderPanelsDialog;
import org.broad.igv.ui.util.HistoryMenu;
import org.broad.igv.ui.util.MenuAndToolbarUtils;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.ui.util.TabsMenu;
import org.broad.igv.ui.util.UIUtilities;
import org.broad.igv.util.BrowserLauncher;

/**
 * @author jrobinso
 * @date Apr 4, 2011
 */
public class IGVMenuBar extends JMenuBar {

    private static Logger log = Logger.getLogger(IGVMenuBar.class);

    private JMenu extrasMenu;
    private RemoveUserDefinedGenomeMenuAction removeImportedGenomeAction;
    private FilterTracksMenuAction filterTracksAction;
    private JMenu viewMenu;
    
    private JMenu removeAttributeOriginMenu;

	static private boolean showDebugMenu = false;


    public IGVMenuBar() {
        setBorder(new BasicBorders.MenuBarBorder(Color.GRAY, Color.GRAY));
        setBorderPainted(true);

    }

    public void addAllItems()
    {
        for (AbstractButton menu : createMenus()) {
            add(menu);
        }
        add(Box.createHorizontalGlue());
    }
    
    public void removeAllItems()
    {
    	removeAll();
    }
    
    private List<AbstractButton> createMenus() {

        List<AbstractButton> menus = new ArrayList<AbstractButton>();

        menus.add(createFileMenu());
        menus.add(createViewMenu());
        menus.add(createTracksMenu());
        menus.add(createGenomeSpaceMenu());
        menus.add(createHelpMenu());


        extrasMenu = createExtrasMenu();
		if ( PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SHOW_EXTRA_MENU, false) ) 
			extrasMenu.setVisible(true);
        menus.add(extrasMenu);

        return menus;
    }

    public void enableExtrasMenu() {
        extrasMenu.setVisible(true);
    }


    private JMenu createFileMenu() {

        List<JComponent> menuItems = new ArrayList<JComponent>();
        MenuAction menuAction = null;
        JMenuItem menuItem = null;

        menuItems.add(new JSeparator());

        // Load menu items
        menuAction = new LoadFilesMenuAction("Load from File...", KeyEvent.VK_L, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new LoadFromURLMenuAction(LoadFromURLMenuAction.LOAD_FROM_URL, KeyEvent.VK_U, IGV.getInstance());
        JMenuItem menuItem2;
        menuItems.add(menuItem2 = MenuAndToolbarUtils.createMenuItem(menuAction));
        menuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK));
        menuItem2.setMnemonic(KeyEvent.VK_U);

        menuAction = new LoadFromServerAction("Load from Server...", KeyEvent.VK_S, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new LoadFromURLMenuAction(LoadFromURLMenuAction.LOAD_FROM_DAS, KeyEvent.VK_D, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        if(PreferenceManager.getInstance().getAsBoolean(PreferenceManager.DB_ENABLED)) {
            menuAction = new LoadFromDatabaseAction("Load from Database...", 0, IGV.getInstance());
            menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));
        }

        // add unload to attribute files if such
    	removeAttributeOriginMenu = new JMenu("Remove Attribute Origin");
    	menuItems.add(removeAttributeOriginMenu);
    	fillRemoveAttributeOriginMenu();        	
        
        menuItems.add(new JSeparator());

        // Session menu items
        menuAction = new NewSessionMenuAction("New Session...", KeyEvent.VK_N, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new OpenSessionMenuAction("Open Session...", KeyEvent.VK_O, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new SaveSessionMenuAction("Save Session...", KeyEvent.VK_V, IGV.getInstance());
        menuItems.add(menuItem = MenuAndToolbarUtils.createMenuItem(menuAction));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_S);


        menuAction = new GetSessionInfoMenuAction("Get Session Info ...", KeyEvent.VK_I, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new LinkToSessionMenuAction("Link to Session...", KeyEvent.VK_L, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuItems.add(new JSeparator());

        // Load genome
        menuAction =
                new MenuAction(LOAD_GENOME_LIST_MENU_ITEM, null, KeyEvent.VK_I) {

                    @Override
                    public void actionPerformed(ActionEvent event) {

                        SwingWorker worker = new SwingWorker() {

                            public Object doInBackground() {
                                org.broad.igv.ui.util.ProgressMonitor monitor = new org.broad.igv.ui.util.ProgressMonitor();
                                IGV.getInstance().doLoadGenome(monitor);
                                return null;
                            }
                        };
                        worker.execute();
                    }
                };

        menuAction.setToolTipText(LOAD_GENOME_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));


        menuAction =
                new MenuAction(UIConstants.IMPORT_GENOME_LIST_MENU_ITEM, null, KeyEvent.VK_D) {

                    @Override
                    public void actionPerformed(ActionEvent event) {

                        SwingWorker worker = new SwingWorker() {

                            public Object doInBackground() {

                                org.broad.igv.ui.util.ProgressMonitor monitor = new org.broad.igv.ui.util.ProgressMonitor();
                                IGV.getInstance().doDefineGenome(monitor);
                                return null;
                            }
                        };
                        worker.execute();
                    }
                };

        menuAction.setToolTipText(IMPORT_GENOME_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        boolean hasImportedGenomes = true;
        try {
            hasImportedGenomes = !IGV.getInstance().getGenomeManager().getUserDefinedGenomeArchiveList().isEmpty();

        } catch (IOException iOException) {
            // Ignore
        }
        removeImportedGenomeAction = new RemoveUserDefinedGenomeMenuAction(
                UIConstants.REMOVE_GENOME_LIST_MENU_ITEM, KeyEvent.VK_R);
        removeImportedGenomeAction.setEnabled(hasImportedGenomes);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(removeImportedGenomeAction));

        //menuAction = new ClearGenomeCacheAction("Clear Genome Cache...");
        //menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuItems.add(new JSeparator());

        // ***** Snapshots
        // Snapshot Application
        menuAction =
                new MenuAction("Save Image ...", null, KeyEvent.VK_A) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        IGV.getInstance().saveImage(IGV.getInstance().getMainPanel());

                    }
                };

        menuAction.setToolTipText(SAVE_IMAGE_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuItems.add(new JSeparator());

        // Export Regions
        menuAction = new ExportRegionsMenuAction("Export Regions ...", KeyEvent.VK_E, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));


        // Import Regions
        menuAction = new ImportRegionsMenuAction("Import Regions ...", KeyEvent.VK_I, IGV.getInstance());
        menuAction.setToolTipText(IMPORT_REGION_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        // Import Regions
        menuAction = new ClearRegionsMenuAction("Clear Regions ...", IGV.getInstance());
        menuAction.setToolTipText(IMPORT_REGION_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        //dhmay adding 2010/11/16
        // Navigate Regions

        // Separator
        /*menuItems.add(new JSeparator());
        menuAction =
                new MenuAction("Preprocess ...", null, KeyEvent.VK_P) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        (new PreprocessorDialog(IGV.IGV.getInstance(), false)).setVisible(true);
                    }
                };

        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));
        */

        // batch script
        menuItems.add(new JSeparator());

        menuAction = new RunScriptMenuAction("Run Batch Script...", KeyEvent.VK_X, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        // igvtools

        menuItems.add(new JSeparator());
        /*
        menuAction = new SortTracksMenuAction("Compute coverage...", KeyEvent.VK_T, IGV.getInstance()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                CoverageGui.launch(false, IGV.getInstance().getGenomeManager().getGenomeId(), CoverageGui.Mode.COVERAGE);
            }
        };
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new SortTracksMenuAction("Convert to tdf...", KeyEvent.VK_T, IGV.getInstance()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                CoverageGui.launch(false, IGV.getInstance().getGenomeManager().getGenomeId(), CoverageGui.Mode.TILE);
            }
        };
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));


        menuAction = new SortTracksMenuAction("Create index...", KeyEvent.VK_T, IGV.getInstance()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                IndexGui.launch(false);
            }
        };
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));
        */

        menuAction = new SortTracksMenuAction("Run igvtools...", KeyEvent.VK_T, IGV.getInstance()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                IgvToolsGui.launch(false, IGV.getInstance().getGenomeManager().getGenomeId());
            }
        };
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));


        // TODO -- change "Exit" to "Close" for BioClipse
        menuItems.add(new JSeparator());      // Exit
        menuAction =
                new MenuAction("Exit", null, KeyEvent.VK_X) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doExitApplication();
                    }
                };

        menuAction.setToolTipText(EXIT_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));


        // Empty the recent sessions list before we start to do
        // anything with it
        IGV.getInstance().getRecentSessionList().clear();

        // Retrieve the stored session paths
        String recentSessions = PreferenceManager.getInstance().getRecentSessions();
        if (recentSessions != null) {
            String[] sessions = recentSessions.split(";");
            for (String sessionPath : sessions) {
                if (!IGV.getInstance().getRecentSessionList().contains(sessionPath)) {
                    IGV.getInstance().getRecentSessionList().add(sessionPath);
                }

            }
        }

        if (!IGV.getInstance().getRecentSessionList().isEmpty()) {

            menuItems.add(new JSeparator());

            // Now add menu items
            for (final String session : IGV.getInstance().getRecentSessionList()) {
                OpenSessionMenuAction osMenuAction = new OpenSessionMenuAction(session, new File(session), IGV.getInstance());
                menuItems.add(MenuAndToolbarUtils.createMenuItem(osMenuAction));
            }

        }

        @SuppressWarnings("serial")
		MenuAction fileMenuAction = new MenuAction("File", null, KeyEvent.VK_F);
        
        final JMenu		menu = MenuAndToolbarUtils.createMenu(menuItems, fileMenuAction);
        
        menu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent arg0) {
				fillRemoveAttributeOriginMenu();
				menu.revalidate();
				menu.repaint();
				menu.doClick();
			}
			
			@Override
			public void menuDeselected(MenuEvent arg0) {
			}
			
			@Override
			public void menuCanceled(MenuEvent arg0) {
			}
		});
        
        return menu;
    }

    private void fillRemoveAttributeOriginMenu() {
    	

		removeAttributeOriginMenu.removeAll();
    	
        List<String>		origins = AttributeManager.getInstance().getAllAttributeOrigins();

        for ( String origin : origins )
    	{
    		File		file = new File(origin);
    		@SuppressWarnings("serial")
			MenuAction	action = new MenuAction(file.getName()) {

				@Override
				public void actionPerformed(ActionEvent event) {
					Object			source = event.getSource();
					if ( source instanceof JMenuItem )
					{
						JMenuItem		item = (JMenuItem)source;
						String			path = item.getToolTipText();
						AttributeManager.getInstance().removeAttributeOrigin(path);
						
						IGV.getInstance().getMainPanel().repaint();
						IGV.getInstance().repaint();
					}
				}
    		};
    		JMenuItem		item = new JMenuItem(action);
    		item.setToolTipText(file.getAbsolutePath());
    		removeAttributeOriginMenu.add(item);
    	}

        removeAttributeOriginMenu.setEnabled(origins.size() > 0);
	}

	private JMenu createTracksMenu() {

        List<JComponent> menuItems = new ArrayList<JComponent>();
        MenuAction menuAction = null;

        // Sort Context
        menuAction = new SortTracksMenuAction("Sort Tracks ...", KeyEvent.VK_S, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new GroupTracksMenuAction("Group Tracks  ... ", KeyEvent.VK_G, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        // Filter Tracks
        filterTracksAction = new FilterTracksMenuAction("Filter Tracks ...", KeyEvent.VK_F, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(filterTracksAction));

        menuItems.add(new JSeparator());

        // Reset Tracks
        menuAction = new FitDataToWindowMenuAction("Fit Data to Window", KeyEvent.VK_W, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));


        // Set track height
        menuAction = new SetTrackHeightMenuAction("Set Track Height...", KeyEvent.VK_H, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        // create a new panel
        menuItems.add(new JSeparator());
        menuAction = new NewPanelMenuAction("New Panel", KeyEvent.VK_P, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));
        
        // delete (empty) panel(s)
        menuAction = new DeletePanelsMenuAction("Purge Empty Panels", KeyEvent.VK_D, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        MenuAction dataMenuAction = new MenuAction("Tracks", null, KeyEvent.VK_K);
        return MenuAndToolbarUtils.createMenu(menuItems, dataMenuAction);
    }


    private JMenu createViewMenu() {

        List<JComponent> menuItems = new ArrayList<JComponent>();
        MenuAction menuAction = null;

        // Preferences
        menuAction =
                new MenuAction("Preferences...", null, KeyEvent.VK_P) {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        UIUtilities.invokeOnEventThread(new Runnable() {

                            public void run() {
                                IGV.getInstance().doViewPreferences();
                            }
                        });
                    }
                };
        menuAction.setToolTipText(PREFERENCE_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction =
                new MenuAction("Color Legends ...", null, KeyEvent.VK_H) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        (new LegendDialog(IGV.getMainFrame())).setVisible(true);
                    }
                };
        menuAction.setToolTipText(SHOW_HEATMAP_LEGEND_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new MenuAction("Auto Refresh", null, KeyEvent.VK_U) {

        	public void actionPerformed(ActionEvent e) {

                JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) e.getSource();
                
        		IGV.getInstance().setAutoRefresh(menuItem.isSelected());
            }
        };
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
        menuItem.setSelected(IGV.getInstance().isAutoRefresh());
        menuItem.setAction(menuAction);
        menuItems.add(menuItem);
       
        
        
        menuItems.add(new JSeparator());

        menuAction = new MenuAction("Show Name Panel", null, KeyEvent.VK_A) {
            @Override
            public void actionPerformed(ActionEvent e) {

                JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) e.getSource();
                if (menuItem.isSelected()) {
                    IGV.getInstance().getMainPanel().expandNamePanel();
                } else {
                    IGV.getInstance().getMainPanel().collapseNamePanel();
                }
                IGV.getInstance().doRefresh();
            }
        };
        boolean isShowing = IGV.getInstance().getMainPanel().isExpanded();
        menuItem = new JCheckBoxMenuItem();
        menuItem.setSelected(isShowing);
        menuItem.setAction(menuAction);
        menuItems.add(menuItem);

        JMenuItem panelWidthmenuItem = new JMenuItem();
        menuAction = new MenuAction("Set Name Panel Width...", null, KeyEvent.VK_A) {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel mainPanel = IGV.getInstance().getMainPanel();
                String currentValue = String.valueOf(mainPanel.getNamePanelWidth());
                String newValue = MessageUtils.showInputDialog("Enter track name panel width: ", currentValue);
                if (newValue != null) {
                    try {
                        Integer w = Integer.parseInt(newValue);
                        if (w <= 0 || w == 1000) throw new NumberFormatException();
                        PreferenceManager.getInstance().put(PreferenceManager.NAME_PANEL_WIDTH, newValue);
                        
                        // loop over all panels
                        for ( MainPanel mp : IGV.getInstance().getContentPane().tabsMapMainPanels().values() )
                        	mp.setNamePanelWidth(w);
                    } catch (NumberFormatException ex) {
                        MessageUtils.showMessage("Error: value must be a positive integer < 1000.");
                    }
                }
            }
        };
        panelWidthmenuItem.setAction(menuAction);
        menuItems.add(panelWidthmenuItem);

        // Hide or Show the attribute panels
        boolean isShow = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SHOW_ATTRIBUTE_VIEWS_KEY);
        IGV.getInstance().doShowAttributeDisplay(isShow);  // <= WEIRD doing IGV.getInstance() here!

        menuAction = new MenuAction("Show Attribute Display", null, KeyEvent.VK_A) {
            @Override
            public void actionPerformed(ActionEvent e) {

                JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) e.getSource();
                PreferenceManager.getInstance().setShowAttributeView(menuItem.getState());
                IGV.getInstance().getMainPanel().invalidate();
                IGV.getInstance().doRefresh();
            }
        };
        menuAction.setToolTipText(SHOW_ATTRIBUTE_DISPLAY_TOOLTIP);
        menuItem = MenuAndToolbarUtils.createMenuItem(menuAction, isShow);
        menuItems.add(menuItem);


        menuAction =
                new MenuAction("Select Attributes to Show...", null, KeyEvent.VK_S) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        IGV.getInstance().doSelectDisplayableAttribute();
                    }
                };
        menuAction.setToolTipText(SELECT_DISPLAYABLE_ATTRIBUTES_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new MenuAction("Show Header Panel", null, KeyEvent.VK_A) {
            @Override
            public void actionPerformed(ActionEvent e) {

                JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) e.getSource();
                if (menuItem.isSelected()) {
                    IGV.getInstance().getMainPanel().restoreHeader();
                } else {
                    IGV.getInstance().getMainPanel().removeHeader();
                }
                IGV.getInstance().doRefresh();
            }
        };
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction, true));

        menuItems.add(new JSeparator());
        menuAction =
                new MenuAction("Reorder Panels...", null, KeyEvent.VK_S) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ReorderPanelsDialog dlg = new ReorderPanelsDialog(IGV.getMainFrame());
                        dlg.setVisible(true);
                    }
                };
        //menuAction.setToolTipText("");
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));



        //
        menuAction =
                new MenuAction("Gene Lists...", null, KeyEvent.VK_S) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        (new GeneListManagerUI(IGV.getMainFrame())).setVisible(true);
                    }
                };
        menuAction.setToolTipText(SELECT_DISPLAYABLE_ATTRIBUTES_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new NavigateRegionsMenuAction("Region Navigator ...", IGV.getInstance());
        menuAction.setToolTipText(UIConstants.NAVIGATE_REGION_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));
        
        /*
menuAction =
        new MenuAction("Show Region Bars", null, KeyEvent.VK_A) {

            @Override
            public void actionPerformed(ActionEvent e) {

                JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) e.getSource();
                PreferenceManager.getInstance().setShowRegionBars(menuItem.isSelected());
                repaintDataPanels();
            }
        };


        menuItems.add(new JSeparator());
        menuAction =
                new MenuAction("Refresh", null, KeyEvent.VK_R) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        IGV.getInstance().doRefresh();
                    }
                };
        menuAction.setToolTipText(REFRESH_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));
        */

        menuItems.add(new JSeparator());
        menuItems.add(new HistoryMenu("Go to"));
        
        if ( IGVContentPane.isUseTabs() )
        	menuItems.add(new TabsMenu("Tabs"));
        
        // debug menu
        if ( showDebugMenu )
        {
		    JMenu		debugMenu = new JMenu("Debug");
		    menuItems.add(debugMenu);        
		    menuAction = new GetStatusTrackerInfoMenuAction("Get Timing Info ...", KeyEvent.VK_T, IGV.getInstance());
		    debugMenu.add(MenuAndToolbarUtils.createMenuItem(menuAction));
		    menuAction = new GetShowPropsMenuAction("Show Props ...", KeyEvent.VK_T, IGV.getInstance());
		    debugMenu.add(MenuAndToolbarUtils.createMenuItem(menuAction));
        }
        
        menuAction = new SearchMenuAction("Advanced Search ...", KeyEvent.VK_L, IGV.getInstance());
        JMenuItem		item;
        menuItems.add(item = MenuAndToolbarUtils.createMenuItem(menuAction));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));
        item.setMnemonic(KeyEvent.VK_L);
        

        // Add to IGVPanel menu
        MenuAction dataMenuAction = new MenuAction("View", null, KeyEvent.VK_V);
        viewMenu = MenuAndToolbarUtils.createMenu(menuItems, dataMenuAction);
        return viewMenu;
    }


    private JMenu createHelpMenu() {

        List<JComponent> menuItems = new ArrayList<JComponent>();

        MenuAction menuAction = null;

        menuAction =
                new MenuAction("User Guide ... ") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            BrowserLauncher.openURL(SERVER_BASE_URL + "igv/UserGuide");
                        } catch (IOException ex) {
                            log.error("Error opening browser", ex);
                        }

                    }
                };
        menuAction.setToolTipText(HELP_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));


        menuAction =
                new MenuAction("Tutorial ... ") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            BrowserLauncher.openURL(SERVER_BASE_URL + "igv/QuickStart");
                        } catch (IOException ex) {
                            log.error("Error opening browser", ex);
                        }

                    }
                };
        menuAction.setToolTipText(TUTORIAL_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        if (Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.MAIL)) {

                menuAction =
                        new MenuAction("Help Forum...") {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    URI uri = new URI("http://groups.google.com/forum/#!forum/igv-help");
                                    Desktop.getDesktop().browse(uri);
                                } catch (Exception ex) {
                                    log.error("Error opening igv-help uri", ex);
                                }

                            }
                        };
                menuAction.setToolTipText("Email support");
                menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));
            }
        }

        menuAction =
                new MenuAction("About IGV ") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        (new AboutDialog(IGV.getMainFrame(), true)).setVisible(true);
                    }
                };
        menuAction.setToolTipText(ABOUT_TOOLTIP);
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        MenuAction toolMenuAction = new MenuAction("Help");
        return MenuAndToolbarUtils.createMenu(menuItems, toolMenuAction);
    }

    private JMenu createGenomeSpaceMenu() {

        JMenu menu = new JMenu("GenomeSpace");

        MenuAction menuAction = null;
        menuAction = new LoadFromGSMenuAction("Load from GenomeSpace...", KeyEvent.VK_U, IGV.getInstance());
        menu.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menu.addSeparator();

        menuAction = new GSSaveSessionMenuAction("Save session to GenomeSpace...", IGV.getInstance());
        menu.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuAction = new GSOpenSessionMenuAction("Load session from GenomeSpace...", IGV.getInstance());
        menu.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menu.add(new JSeparator());
        menuAction = new MenuAction("Logout") {
            @Override
            public void actionPerformed(ActionEvent e) {
                GSUtils.logout();
            }
        };
        menu.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menu.setVisible(PreferenceManager.getInstance().getAsBoolean(PreferenceManager.GENOME_SPACE_ENABLE));


        return menu;
    }

    private JMenu createExtrasMenu() {

        List<JComponent> menuItems = new ArrayList<JComponent>();

        MenuAction menuAction = null;

        menuAction = new LoadFromGSMenuAction("Load from GenomeSpace...", KeyEvent.VK_U, IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        // Preferences reset
        menuAction = new ResetPreferencesAction("Reset Preferences", IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        menuItems.add(new JSeparator());
        menuAction = new MenuAction("Variant list ...  *EXPERIMENTAL*") {
            @Override
            public void actionPerformed(ActionEvent e) {
                VariantListManager.openNavigator(IGV.getMainFrame());
            }
        };
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));


        menuItems.add(new JSeparator());


        // Set frame dimensions
        menuAction =
                new MenuAction("Set window dimensions", null, KeyEvent.VK_C) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String value = JOptionPane.showInputDialog("Enter dimensions, e.g. 800x400");
                        String[] vals = value.split("x");
                        if (vals.length == 2) {
                            int w = Integer.parseInt(vals[0]);
                            int h = Integer.parseInt(vals[1]);
                            IGV.getMainFrame().setSize(w, h);
                        }
                    }
                };
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        // Save entire window
        menuAction =
                new MenuAction("Save Screenshot ...", null, KeyEvent.VK_A) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        IGV.getInstance().saveImage(IGV.getInstance().getContentPane());

                    }
                };

        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        //
        JMenu lfMenu = new JMenu("L&F");
        LookAndFeel lf = UIManager.getLookAndFeel();
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {

            final String lfName = info.getName();
            JMenuItem cb = new JMenuItem(lfName);
            //cb.setSelected(info.getClassName().equals(lf.getClass().getName());
            cb.addActionListener(new AbstractAction() {

                public void actionPerformed(ActionEvent actionEvent) {
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {

                        if (lfName.equals(info.getName())) {
                            try {
                                UIManager.setLookAndFeel(info.getClassName());
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (InstantiationException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (UnsupportedLookAndFeelException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            break;
                        }
                    }
                }
            });
            lfMenu.add(cb);
        }

        menuAction = new ExportTrackNamesMenuAction("Export track names...", IGV.getInstance());
        menuItems.add(MenuAndToolbarUtils.createMenuItem(menuAction));

        MenuAction extrasMenuAction = new MenuAction("Extras");
        JMenu menu = MenuAndToolbarUtils.createMenu(menuItems, extrasMenuAction);

        menu.add(lfMenu);

        menu.setVisible(false);


        return menu;
    }


    public void enableRemoveGenomes() {
        if (removeImportedGenomeAction != null) {
            removeImportedGenomeAction.setEnabled(true);
        }

    }

    public void resetSessionActions() {
        if (filterTracksAction != null) {
            filterTracksAction.resetTrackFilter();
        }
    }


    public void setFilterMatchAll(boolean value) {
        if (filterTracksAction != null) {
            filterTracksAction.setFilterMatchAll(value);
        }

    }

    public boolean isFilterMatchAll() {
        if (filterTracksAction != null) {
            return filterTracksAction.isFilterMatchAll();
        }

        return false;
    }

    public void setFilterShowAllTracks(boolean value) {
        if (filterTracksAction != null) {
            filterTracksAction.setFilterShowAllTracks(value);
        }

    }

    public boolean isFilterShowAllTracks() {
        if (filterTracksAction != null) {
            return filterTracksAction.getShowAllTracksFilterCheckBox().isSelected();
        }

        return false;
    }

    public JMenu getViewMenu() {
        return viewMenu;
    }

    final public void doExitApplication() {

        try {
            IGV igv = IGV.getInstance();

            // Store recent sessions
            final LinkedList<String> recentSessionList = igv.getRecentSessionList();
            if (!recentSessionList.isEmpty()) {

                int size = recentSessionList.size();
                if (size > UIConstants.NUMBER_OF_RECENT_SESSIONS_TO_LIST) {
                    size = UIConstants.NUMBER_OF_RECENT_SESSIONS_TO_LIST;
                }

                String recentSessions = "";
                for (int i = 0; i <
                        size; i++) {
                    recentSessions += recentSessionList.get(i);

                    if (i < (size - 1)) {
                        recentSessions += ";";
                    }

                }
                PreferenceManager.getInstance().remove(PreferenceManager.RECENT_SESSION_KEY);
                PreferenceManager.getInstance().setRecentSessions(recentSessions);
            }

            // Save application location and size
            PreferenceManager.getInstance().setApplicationFrameBounds(IGV.getMainFrame().getBounds());

            // Hide and close the application
            IGV.getMainFrame().setVisible(false);
            IGV.getMainFrame().dispose();

        } finally {
            System.exit(0);
        }

    }
}

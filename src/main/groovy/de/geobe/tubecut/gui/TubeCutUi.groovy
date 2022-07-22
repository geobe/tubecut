package de.geobe.tubecut.gui

import groovy.swing.SwingBuilder

import javax.swing.*
import java.awt.*

import static java.awt.GridBagConstraints.EAST
import static java.awt.GridBagConstraints.HORIZONTAL
import static javax.swing.JFrame.EXIT_ON_CLOSE

/**
 * a simple swing user interface for the tubecut program
 */
class TubeCutUi {
    SwingBuilder swing = new SwingBuilder()
    JFrame frame
    JSplitPane splitPane
    JPanel values, defaults, drawing
    JTabbedPane inputTabs
    def x = new JTextField(5)

    static void main(String[] args) {
        def ui = new TubeCutUi()
        ui.buildUi()
    }

    def gbcLabel = { int x, int y ->
        [gridx: x, gridy: y, anchor: EAST, ipadx: 5, ipady: 2]
    }

    def buildUi() {
        def i18n = new TCi18n()
        i18n.setLoc('en')
        swing.edt {
            menuBar() {
                menu(text: 'whoops')
            }
            frame = swing.frame(title: 'TubeCut', pack: true, show: true,
                    preferredSize: [1200, 800], location: [500, 500],
                    defaultCloseOperation: EXIT_ON_CLOSE) {
                splitPane = splitPane(dividerLocation: 300) {
                    inputTabs = tabbedPane(selectedIndex: 0) {
                        values = panel(name: i18n.v('values'), toolTipText: i18n.v('values_tt')) {
                            gridBagLayout()
                            label(text: i18n.v('ltube'), toolTipText: i18n.v('ltube_tt'), constraints: gbc(gbcLabel(0, 0)))
                            textField(columns: 8, constraints: gbc(gridx: 1, gridy: 0, ipadx: 50, ipady: 2, fill: HORIZONTAL))
                            label(text: i18n.v('stube'), toolTipText: i18n.v('stube_tt'), constraints: gbc(gbcLabel(0, 1)))
                            textField(columns: 8, constraints: gbc(gridx: 1, gridy: 1, ipadx: 5, ipady: 1, fill: HORIZONTAL))
                            label(text: i18n.v('angle'), toolTipText: i18n.v('angle_tt'), constraints: gbc(gbcLabel(0, 2)))
                            textField(columns: 8, constraints: gbc(gridx: 1, gridy: 2, ipadx: 5, ipady: 1, fill: HORIZONTAL))
                            label(text: i18n.v('exc'), toolTipText: i18n.v('exc_tt'), constraints: gbc(gbcLabel(0, 3)))
                            textField(columns: 8, constraints: gbc(gridx: 1, gridy: 3, ipadx: 5, ipady: 1, weightx: 0.0, weighty: 0.0, fill: HORIZONTAL))
                            button(text: i18n.v('calc_bt'), toolTipText: i18n.v('calc_bt_tt'),
                                    constraints: gbc(gridx: 0, gridy: 4, ipadx: 2, ipady: 1, weightx: 0.0, weighty: 1.0))
                        }
                        defaults = panel(name: i18n.v('defaults'), toolTipText: i18n.v('defaults_tt')) {
                            label(text: i18n.v('pagew'), toolTipText: i18n.v('pagew_tt') )
                            textField(columns: 8, text: '291')
                        }
                    }
                    drawing = panel(name: 'drawing', preferredSize: [400, 300], background: Color.LIGHT_GRAY)
                }
            }
        }
//        frame.setLocation(500, 500)

    }
}

/**
 * a very simple service class supporting i18n
 */
class TCi18n {
    def loc = 'de'
    def locs = ['de', 'en']
    def locix = 0

    void setLoc(String l) {
        def ix = locs.indexOf(l)
        if (ix >= 0) {
            loc = l
            locix = ix
        } else {
            loc = locs[0]
            locix = 0
        }
    }

    def trans = [
            values     : ['Eingabewerte', 'Input Values'],
            values_tt  : ['Werte für die aktuelle Berechnung', 'values for actual calculation'],
            defaults   : ['Standardwerte', 'Default values'],
            defaults_tt: ['Standardwerte für Papiergröße, Dateinamen usw.', 'defaults for paper size, file name patterns etc'],
            ltube      : ['Ø großes Rohr [mm]', 'Ø large tube [mm]'],
            ltube_tt   : ['Außendurchmesser des großen Rohres in mm', 'outer diameter of large tube in mm'],
            stube      : ['Ø großes Rohr [mm]', 'Ø small tube [mm]'],
            stube_tt   : ['Außendurchmesser des kleinen Rohres in mm', 'outer diameter of small tube in mm'],
            angle      : ['Winkel [°]', 'Angle [°]'],
            angle_tt   : ['Winkel zwischen den Rohren in Grad', 'angle between tubes in degrees'],
            exc        : ['Exzentrizität [mm]', 'Excentricity [mm]'],
            exc_tt     : ['Abstand zwischen den Mittelachsen der Rohre in mm', ' distance between center axis ob tubes in mm'],
            calc_bt    : ['Berechnen', 'Calculate'],
            calc_bt_tt : ['Berechnet die Schnittkurven für die angegebenen Werte', 'calculate cutting curves for given values'],
            save_bt    : ['Speichern', 'Save'],
            save_bt_tt : ['Schnittkurven als svg Datei speichern, die im Browser angezeigt und gedruckt werden kann',
                          'save cutting curves as svg file that can be shown and printed in a browser'],
            pagew      : ['Seitenbreite', 'Page Width'],
            pagew_tt   : ['Seitenbreite (Querformat) in mm', 'landscape page width'],
            pageh      : [],
            pageh_tt   : []
    ]

    def v(def key) {
        def st = trans[key]
        st?.size() > locix ? st[locix] : (st ? st[0] : key)
    }
}
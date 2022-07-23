package de.geobe.tubecut.gui

import de.geobe.tubecut.calc.SvgGenerator
import groovy.swing.SwingBuilder

import javax.swing.*
import java.awt.*

import static java.awt.GridBagConstraints.*
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
    SpinnerNumberModel dlarge, dsmall, angle, excentricity, pagew, pageh
    JTextField filenamePattern, plotDir
    JTextArea svgText
    Action calcAction, saveAction
    String svg
    def i18n = new TCi18n()
    def x = new JTextField(5);

    {
        dlarge = new SpinnerNumberModel(value: 156, minimum: 1, maximum: 250)
        dsmall = new SpinnerNumberModel(value: 40, minimum: 1, maximum: 100)
        angle = new SpinnerNumberModel(value: 40, minimum: 15, maximum: 90)
        excentricity = new SpinnerNumberModel(value: 55, minimum: 0, maximum: 120)
        pagew = new SpinnerNumberModel(value: 291, minimum: 105, maximum: 420)
        pageh = new SpinnerNumberModel(value: 210, minimum: 105, maximum: 420)
    }

    static void main(String[] args) {
        def ui = new TubeCutUi()
        ui.buildUi()
    }

    def gbcLabel = { int x, int y ->
        [gridx: x, gridy: y, anchor: EAST, ipadx: 8, ipady: 2, weightx: 0.1]
    }

    def gbcText1 = { int y ->
        [gridx: 1, gridy: y, ipadx: 5, ipady: 1, fill: HORIZONTAL, weightx: 0.3]
    }

    def calculateSvg(event) {
        svg = SvgGenerator.calculateTubecutPlot(
                dlarge.value / 2.0,
                dsmall.value / 2.0,
                angle.value,
                excentricity.value,
                0.1,
                pagew.value,
                pageh.value
        )
        if (svg?.length() > 0) {
            saveAction.enabled = true
             svgText.setText(svg)
        } else {
            saveAction.enabled = false
            svgText.setText(i18n.v('nosvg'))
        }
    }

    def saveSvg(event){
        def filename = plotDir.text + '/' + String.format(filenamePattern.text, dlarge.value, dsmall.value, excentricity.value, angle.value)
        new File(filename).withWriter('utf-8') { writer ->
            writer.write(svg)
        }
    }

    def buildUi() {
        def font = new Font('Dialog', Font.BOLD, 15.0)
        i18n.setLoc('de')
        calcAction = swing.action(
                name: i18n.v('calc_bt'),
                closure: this.&calculateSvg,
                mnemonic: 'C',
                accelerator: 'alt C'
        )
        saveAction = swing.action(
                name: i18n.v('save_bt'),
                closure: this.&saveSvg,
                mnemonic: 'S',
                accelerator: 'ctrl s',
                enabled: false
        )
        swing.edt {
            frame = swing.frame(title: 'TubeCut', pack: true, show: true, font: font,
                    preferredSize: [1200, 800], location: [500, 500],
                    defaultCloseOperation: EXIT_ON_CLOSE) {
                menuBar() {
                    menu(text: 'whoops')
                    glue()
                    menu 'Hilfe'
                }
                splitPane = splitPane(dividerLocation: 400) {
                    inputTabs = tabbedPane(selectedIndex: 0, font: font) {
                        values = panel(name: i18n.v('values'), toolTipText: i18n.v('values_tt')) {
                            gridBagLayout()
                            label(text: i18n.v('ltube'), toolTipText: i18n.v('ltube_tt'), font: font,
                                    constraints: gbc(gbcLabel(0, 0)))
                            spinner(font: font, model: dlarge,
                                    constraints: gbc(gbcText1(0)))
                            label(text: i18n.v('stube'), toolTipText: i18n.v('stube_tt'), font: font,
                                    constraints: gbc(gbcLabel(0, 1)))
                            spinner(font: font, model: dsmall,
                                    constraints: gbc(gbcText1(1)))
                            label(text: i18n.v('angle'), toolTipText: i18n.v('angle_tt'), font: font,
                                    constraints: gbc(gbcLabel(0, 2)))
                            spinner(font: font, model: angle,
                                    constraints: gbc(gbcText1(2)))
                            label(text: i18n.v('exc'), toolTipText: i18n.v('exc_tt'), font: font,
                                    constraints: gbc(gbcLabel(0, 3)))
                            spinner(font: font, model: excentricity,
                                    constraints: gbc(gbcText1(3)))
                            glue(constraints: gbc(gridx: 0, gridy: 4, weightx: 0.0, weighty: 1.0))
                            button(action: calcAction, toolTipText: i18n.v('calc_bt_tt'), font: font,
                                    constraints: gbc(gridx: 0, gridy: 5, ipadx: 2, ipady: 1, weightx: 0.0, weighty: 0.1))
                            button(action: saveAction, toolTipText: i18n.v('save_bt_tt'), font: font,
                                    constraints: gbc(gridx: 1, gridy: 5, ipadx: 2, ipady: 1, weightx: 0.0, weighty: 0.1))
                        }
                        defaults = panel(name: i18n.v('defaults'), toolTipText: i18n.v('defaults_tt')) {
                            gridBagLayout()
                            label(text: i18n.v('pagew'), toolTipText: i18n.v('pagew_tt'), font: font,
                                    constraints: gbc(gbcLabel(0, 0)))
                            spinner(font: font, model: pagew,
                                    constraints: gbc(gbcText1(0)))
                            label(text: i18n.v('pageh'), toolTipText: i18n.v('pageh_tt'), font: font,
                                    constraints: gbc(gbcLabel(0, 1)))
                            spinner(font: font, model: pageh,
                                    constraints: gbc(gbcText1(1)))
                            label(text: i18n.v('outputhdr'), toolTipText: i18n.v('pageh_tt'), font: font,
                                    constraints: gbc(gridx: 0, gridy: 2, gridwidth: 2, ipady: 10, anchor: CENTER))
                            label(text: i18n.v('svgdir'), toolTipText: i18n.v('svgdir_tt'), font: font,
                                    constraints: gbc(gbcLabel(0, 3)))
                            plotDir = textField(text: "${System.getProperty('user.home')}/tmp/tmp", font: font,
                                    constraints: gbc(gbcText1(3)))
                            label(text: i18n.v('svgfile'), toolTipText: i18n.v('svgfile_tt'), font: font,
                                    constraints: gbc(gbcLabel(0, 4)))
                            filenamePattern = textField(text: 'tubecut_%d_%d_%d_%d.svg', font: font,
                                    constraints: gbc(gbcText1(4)))
                            glue(constraints: gbc(gridx: 0, gridy: 5, weightx: 0.0, weighty: 1.0))
                        }
                    }
                    drawing = panel(name: 'drawing', preferredSize: [400, 300], background: Color.LIGHT_GRAY) {
                        borderLayout()
                        svgText = textArea(name: svg, text: i18n.v('nosvg'), font: font,
                                constraints: BorderLayout.CENTER)
                    }
                }
            }
        }
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
            pagew      : ['Seitenbreite [mm]', 'Page Width [mm]'],
            pagew_tt   : ['Seitenbreite (Querformat) in mm', 'landscape page width in mm'],
            pageh      : ['Seitenhöhe [mm]', 'Page heigth [mm]'],
            pageh_tt   : ['Seitenhöhe (Querformat) in mm', 'portrait page height in mm'],
            outputhdr  : ['Ausgabedateien', 'Output Files'],
            svgdir     : ['Verzeichnis', 'Folder'],
            svgdir_tt  : ['Die erzeugten SVG Dateien werden in diesem Verzeichnis abgelegt',
                          'created output files are saved into this folder'],
            svgfile    : ['Dateinamenmuster', 'File Name Pattern'],
            svgfile_tt : ['''Dateiname enthält Rohrradius groß, klein, Winkel und Exzentrizität.
Für die Platzhalter %.0f wird der jeweilige Wert eingesetzt''',
                          '''file name contains tube radius large, small, angle and excentricity.
The patterns %.0f are replaced by the current values'''],
            nosvg      : ['noch keine SVG Grafik berechnet oder Berechnung fehlgeschlagen',
                          'no SVG graphic is calculated yet or calculation failed']
    ]

    def v(def key) {
        def st = trans[key]
        st?.size() > locix ? st[locix] : (st ? st[0] : key)
    }
}
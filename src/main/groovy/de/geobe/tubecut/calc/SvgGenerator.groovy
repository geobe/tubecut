package de.geobe.tubecut.calc

import de.geobe.tubecut.gui.TCi18n

import static java.lang.Math.asin

class SvgGenerator {
    int width = 297
    int height = 210
    int translateX = 10
    int translateY = height / 2
    def startPathAt = "M 0,0"

    static trans = [
            fOut   : ["von außen", "from outside"],
            tIn    : ["nach innen", "to inside"],
            large  : ['groß', 'large'],
            small  : ['klein', 'small'],
            len    : ['Länge', 'length'],
            ang    : ['Winkel', 'angle'],
            off    : ['Versatz', 'offset'],
            measure: ['Länge auf Ausdruck nachmessen!', 'Verify length on printout!'],
            base   : ['Seitenlinie (90°)', 'side line (90°)'],
            top    : ['Scheitellinie (180°)', 'top line (180°)']
    ]
    static i18n = new TCi18n(trans: trans)


    String pathFrame(String pathContent) {
        def path = """\
    <path
       style="fill:none;stroke:#000000;stroke-width:0.5px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1"
       d="$pathContent"
    />
"""
        path
    }

    String groupFrame(ArrayList<String> content, int txX = translateX, int txY = translateY, int scX = 1, int scY = -1) {
        StringBuilder builder = new StringBuilder()
        content.each {
            builder.append(it).append('\n')
        }
        def group = """\
  <g
     transform="translate($txX,$txY) scale($scX, $scY)">
     ${builder.toString()}
  </g> """
        group
    }

    String svgFrame(ArrayList<String> groups, int w = width, int h = height) {
        StringBuilder builder = new StringBuilder()
        groups.each {
            builder.append(it).append('\n')
        }
        def svg =
                """\
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<svg
   xmlns:svg="http://www.w3.org/2000/svg"
   xmlns="http://www.w3.org/2000/svg"
   width="${width}mm"
   height="${height}mm"
   viewBox="0 0 $width $height"
   version="1.1">
$groups
</svg>\
"""
        svg
    }

    def writeFile(String svg, def path = '/home/georg/tmp/tmp/ltubecut1.svg') {
        new File(path).withWriter('utf-8') { writer ->
            writer.write(svg)
        }

    }

    def generateSvgPath(TubeCutCentric tc) {
        def path = new StringBuilder()
        path.append startPathAt
        tc.outline.eachWithIndex { PointAt pointAt, int i ->
            if (i == 0) {
                path.append(' L')
            } else {
                path.append(" ${pointAt.len.round(2)},${pointAt.arc.round(2)}")
            }
        }
        tc.outline.eachWithIndex { PointAt pointAt, int i ->
            if (i == 0) {
                path.append(' M 0,0 L')
            } else {
                path.append(" ${pointAt.len.round(2)},${-pointAt.arc.round(2)}")
            }
        }
        path.append("M 0,0 L ${tc.cutLength.round(2)},0")
        path.toString()
    }

    def generateLegend(dlarge, dsmall, angle, offset, length) {
        int xText = length / 3
        i18n.with {
            """\
    <text x="$xText" y="10" style="font-family:sans-serif;font-size:1mm">
        <tspan>D<tspan style="baseline-shift:sub;font-size:0.8mm">${v('large')}</tspan> = $dlarge mm,</tspan>
        <tspan>  D<tspan style="baseline-shift:sub;font-size:0.8mm">${v('small')}</tspan> = $dsmall mm,</tspan>
        <tspan>  ${v('ang')} = $angle°,</tspan>
        <tspan>  ${v('off')} = $offset mm,</tspan>
        <tspan>  ${v('len')} = ${length.round(2)} mm</tspan>
        <tspan x="${xText}" y="18" style="font-weight:bold">${v('measure')}</tspan>
    </text>
"""
        }
    }

    def generateLine(String text, String color, ArrayList points, sx = 1, sy = -1) {
        def line = """\
<line x1='${points[0]}' y1='${points[1]}' x2='${points[2]}' y2='${points[3]}' \
style='stroke:$color;stroke-width:0.4mm;' />
"""
        def tx = (points[0] + points[2]) / 2
        def ty = ((points[1] + points[3]) / 2) + 4
        def legend = generateTextGroup(text, tx, ty, sx, sy)
        line + legend
    }

    def generateTextGroup(text, tx, ty, sx, sy) {
        """\
  <g
     transform="translate($tx,$ty) scale($sx, $sy)">
     <text x="0" y="0" style="font-family:sans-serif;font-size:1mm">
        $text
     </text>
  </g>"""
    }

    def pointsToPath(ArrayList<Point> points0) {
        StringBuilder builder = new StringBuilder()
        builder.append()
        points0.eachWithIndex { Point point, int ix ->
            if (ix == 0) {
                builder.append " M $point.x,$point.y".toString()
            } else {
                builder.append " L $point.x,$point.y".toString()
            }
        }
        builder.toString()
    }

    def pointsAtToPath(ArrayList<PointAt> points0, ArrayList<PointAt> points1) {
        StringBuilder builder = new StringBuilder()
        builder.append " M ${points1[0].len.round(2)},${points1[0].arc.round(2)} L".toString()
        points0.each { PointAt point ->
            builder.append " ${point.len.round(2)},${point.arc.round(2)}".toString()
        }
        builder.append " ${points1[-1].len.round(2)},${points1[-1].arc.round(2)}".toString()
        points1.eachWithIndex { PointAt point, int i ->
            if (i == 0) {
                builder.append " M ${point.len.round(2)},${point.arc.round(2)} L".toString()
            } else {
                builder.append " ${point.len.round(2)},${point.arc.round(2)}".toString()
            }
        }
        builder.toString()
    }

    static calculateTubecutPlot(
            double rLarge = 78.0,
            double rSmall = 20.0,
            double angle = 45.0,
            double exc = 55.0,
            double stepsize = 0.1,
            int pageWidth = 297,
            int pageHeight = 210,
            int xOffset = 20
//            String plotDir = '/tmp/tmp',
//            String filenamePattern = 'ltubecut_%.0f_%.0f_%.0f_%.0f.svg'
    ) {
        def cutline = asin(exc / rLarge) * rLarge
        def gen = new SvgGenerator()
        def calc = new TubeCutCalculatorExcentric(rSmall, rLarge, angle, exc)
        // cut graph
        def tc = calc.calculate(stepsize)
        def length = gen.generateLine(i18n.v('len'), 'red', [tc.cutStart, cutline, tc.cutEnd, cutline], 1, -1)
        def length2 = gen.generateLine(i18n.v('len'), 'red', [tc.cutStart, cutline, tc.cutEnd, cutline], -1, -1)
//        def length = /<line x1="${tc.cutStart}" y1="$cutline" x2="${tc.cutEnd}" y2="$cutline" style="stroke:red;stroke-width:0.2mm;" \/>/
        def zenith = rLarge * Math.PI / 2
//        def zenithLine = /<line x1="0" y1="${zenith}" x2="${pageWidth}" y2="${zenith}" style="stroke:blue;stroke-width:0.4mm;" \/>/
        def zenithLine = gen.generateLine(i18n.v('top'), 'blue', [0, zenith, pageWidth, zenith])
//        def baseline = /<line x1="0" y1="0" x2="${pageWidth}" y2="0" style="stroke:green;stroke-width:0.4mm;" \/>/
        def baseline = gen.generateLine(i18n.v('base'), 'green', [0, 0, pageWidth, 0])
        def pEntry0 = gen.pointsAtToPath(tc.outlines[0], tc.outlines[1])
        def p0 = gen.pathFrame(pEntry0)
        def legend = gen.generateLegend(
                rLarge * 2,
                rSmall * 2,
                angle,
                exc,
                tc.cutLength).toString()
        def tg0 = gen.generateTextGroup(i18n.v('tIn'), tc.cutStart, tc.cutTop + 2, 1, -1)
        def tg1 = gen.generateTextGroup(i18n.v('fOut'), tc.cutEnd - xOffset, tc.cutBottom - 5, 1, -1)
        def tg0r = gen.generateTextGroup(i18n.v('tIn'), tc.cutStart + xOffset, tc.cutTop + 2, -1, -1)
        def tg1r = gen.generateTextGroup(i18n.v('fOut'), tc.cutEnd, tc.cutBottom - 5, -1, -1)
        def group = gen.groupFrame([p0, length, zenithLine, baseline, tg0, tg1], -(int) tc.cutStart + 10, pageHeight - 50)
        def group2 = gen.groupFrame([p0, length2, tg0r, tg1r], pageWidth - xOffset, pageHeight - 50, -1)
        def textgroup = gen.groupFrame([legend], xOffset, 0, 1, 1)
        def svg = gen.svgFrame([group, group2, textgroup])
        svg
    }

    static void main(String[] args) {
        calculateTubecutPlot()
        def trans = [
                fOut: ["von außen", "from outside"],
        ]
        def i18n = new TCi18n()
    }
}

package de.geobe.tubecut.calc

import de.geobe.tubecut.gui.TCi18n

import static java.lang.Math.PI
import static java.lang.Math.asin

class SvgGenerator {
    int width = 297
    int height = 210
    int translateX = 10
    int translateY = height / 2
    def startPathAt = "M 0,0"
    static final rad2deg = 180.0 / PI
    static final deg2rad = PI / 180

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
            top    : ['Scheitellinie (180°)', 'top line (180°)'],
            solid  : ['Durchgezogene Linie ist ideale Schnittkurve auf der inneren Rohroberfläche',
                      'solid line is ideal cutting shape on inner tube surface'],
            dashed : ['Gestrichelte Linie ist ideale Schnittkurve auf der äußeren Rohroberfläche',
                      'dashed line is ideal cutting shape on outer tube surface'],
    ]
    static i18n = new TCi18n(trans: trans)


    String pathFrame(String pathContent, boolean dash = false, ArrayList transform = [0, 0]) {
        def dashline = ''
        if (dash) {
            dashline = ';stroke-dasharray:1%,1%'
        }
        def path = """\
    <path transform="translate(${transform[0]},${transform[1]})"
       style="fill:none;stroke:#000000;stroke-width:0.5px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1$dashline"
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

    def generateLegend(dlarge, dsmall, angle, offset, length, pageheight) {
        int xText = length / 3
        int texttop = pageheight - 40
        int ty = texttop
        i18n.with {
            """\
    <text x="$xText" y="${ty}" style="font-family:sans-serif;font-size:1mm">
        <tspan>D<tspan style="baseline-shift:sub;font-size:0.8mm">${v('large')}</tspan> = $dlarge mm,</tspan>
        <tspan>  D<tspan style="baseline-shift:sub;font-size:0.8mm">${v('small')}</tspan> = $dsmall mm,</tspan>
        <tspan>  ${v('ang')} = $angle°,</tspan>
        <tspan>  ${v('off')} = $offset mm,</tspan>
        <tspan>  ${v('len')} = ${length.round(2)} mm</tspan>
        <tspan x="${xText}" y="${ty += 8}">${v('solid')}</tspan>
        <tspan x="${xText}" y="${ty += 6}">${v('dashed')}</tspan>
        <tspan x="${xText}" y="${ty += 6}" style="font-weight:bold">${v('measure')}</tspan>
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

    def pointsAtToPath(ArrayList<PointAt> points0, ArrayList<PointAt> points1, double deltaX = 0.0) {
        StringBuilder builder = new StringBuilder()
        builder.append " M ${points1[0].len.round(2) + deltaX},${points1[0].arc.round(2)} L".toString()
        points0.each { PointAt point ->
            builder.append " ${point.len.round(2) + deltaX},${point.arc.round(2)}".toString()
        }
        builder.append " ${points1[-1].len.round(2) - deltaX},${points1[-1].arc.round(2)}".toString()
        points1.eachWithIndex { PointAt point, int i ->
            if (i == 0) {
                builder.append " M ${point.len.round(2) - deltaX},${point.arc.round(2)} L".toString()
            } else {
                builder.append " ${point.len.round(2) - deltaX},${point.arc.round(2)}".toString()
            }
        }
        builder.toString()
    }

    static calculateTubecutPlot(
            double rLarge = 75.0,
            double thlarge = 5.0,
            double rSmall = 20.0,
            double angle = 45.0,
            double offset = 55.0,
            double stepsize = 0.1,
            int pageWidth = 297,
            int pageHeight = 210,
            int xOffset = 5
    ) {
        def offsetAngle = asin(offset / rLarge)
        def cutlineOuter = offsetAngle * (rLarge + thlarge)
        def gen = new SvgGenerator()
        // cut graph for tube inside
        def calc = new TubeCutCalculatorExcentric(rSmall, rLarge, angle, offset)
        def tcInner = calc.calculate(stepsize)
        // cut graph for tube outside
        def calcOuter = new TubeCutCalculatorExcentric(rSmall, rLarge + thlarge, angle, offset)
        def tcOuter = calcOuter.calculate(stepsize)
        def length = gen.generateLine(i18n.v('len'),
                'red', [tcInner.cutStart, cutlineOuter, tcInner.cutEnd, cutlineOuter], 1, -1)
        def length2 = gen.generateLine(i18n.v('len'),
                'red', [tcInner.cutStart, cutlineOuter, tcInner.cutEnd, cutlineOuter], -1, -1)
        // plot iss to be fixed on uoter surface, so take outer radius including wall thickness
        def zenith = (rLarge + thlarge) * PI / 2
        // line marks top of large tube (180°)
        def zenithLine = gen.generateLine(i18n.v('top'), 'blue', [0, zenith, pageWidth, zenith])
        // line marks middle of larger tube (90°)
        def midline = gen.generateLine(i18n.v('base'), 'green', [0, 0, pageWidth, 0])
        // entry shape at inner surface of large tube
        def pEntryInner = gen.pointsAtToPath(tcInner.outlines[0], tcInner.outlines[1])
        def pInner = gen.pathFrame(pEntryInner)
        // entry shape at outer surface of large tube
        def pEntryOuter = gen.pointsAtToPath(tcOuter.outlines[0], tcOuter.outlines[1])
        def pOuter = gen.pathFrame(pEntryOuter, true, [0, 0.0])
        def legend = gen.generateLegend(
                rLarge * 2, rSmall * 2, angle, offset, tcInner.cutLength, pageHeight
        ).toString()
        def tg0 = gen.generateTextGroup(i18n.v('tIn'), tcInner.cutStart, tcInner.cutTop + 2, 1, -1)
        def tg1 = gen.generateTextGroup(i18n.v('fOut'), tcInner.cutEnd - xOffset, tcInner.cutBottom - 5, 1, -1)
        def tg0r = gen.generateTextGroup(i18n.v('tIn'), tcInner.cutStart + 3 * xOffset, tcInner.cutTop + 2, -1, -1)
        def tg1r = gen.generateTextGroup(i18n.v('fOut'), tcInner.cutEnd + 2 * xOffset, tcInner.cutBottom - 5, -1, -1)
        def group = gen.groupFrame([pInner, pOuter, length, zenithLine, midline, tg0, tg1], -(int) tcInner.cutStart + xOffset, pageHeight - 50)
        def group2 = gen.groupFrame([pInner, pOuter, length2, tg0r, tg1r], pageWidth - xOffset, pageHeight - 50, -1)
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

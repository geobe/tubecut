package de.geobe.tubecut.calc

class SvgGenerator {
    int width = 297
    int height = 210
    int translateX = 10
    int translateY = height / 2
    def startPathAt = "M 0,0"

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

    def generateLegend(dlarge, dsmall, angle, length) {
        int xText = length / 3
        """\
    <text x="$xText" y="10" style="font-family:sans-serif;font-size:1mm">
        <tspan>D<tspan style="baseline-shift:sub;font-size:0.8mm">groß</tspan> = $dlarge mm,</tspan>
        <tspan>  D<tspan style="baseline-shift:sub;font-size:0.8mm">klein</tspan> = $dsmall mm,</tspan>
        <tspan>  Winkel = $angle°,</tspan>
        <tspan>  Länge = ${length.round(2)} mm</tspan>
        <tspan x="${xText}" y="18" style="font-weight:bold">Länge auf Ausdruck nachmessen!</tspan>
    </text>
"""
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

    static void calculateTubecutPlot(
            double rLarge = 78.0,
            double rSmall = 20.0,
            double angle = 45.0,
            double exc = 55.0,
            double stepsize = 0.1,
            int pageWidth = 297,
            int pageHeight = 210,
            int xOffset = 20,
            String plotDir = '/tmp/tmp',
            String filenamePattern = 'ltubecut_%.0f_%.0f_%.0f_%.0f.svg'
    ) {
        def gen = new SvgGenerator()
        def calc = new TubeCutCalculatorExcentric(rSmall, rLarge, angle, exc)
        // cut graph
        def tc = calc.calculate(stepsize)
        def length = /<line x1="${tc.cutStart}" y1="$exc" x2="${tc.cutEnd}" y2="$exc" style="stroke:red;stroke-width:0.2mm;" \/>/
        def zenith = calc.largeTubeRadius * Math.PI / 2
        def zenithLine = /<line x1="0" y1="${zenith}" x2="${pageWidth}" y2="${zenith}" style="stroke:blue;stroke-width:0.4mm;" \/>/
        def baseline = /<line x1="0" y1="0" x2="${pageWidth}" y2="0" style="stroke:green;stroke-width:0.4mm;" \/>/
        def pEntry0 = gen.pointsAtToPath(tc.outlines[0], tc.outlines[1])
        def p0 = gen.pathFrame(pEntry0)
        def legend = gen.generateLegend(
                calc.largeTubeRadius * 2,
                calc.smallTubeRadius * 2,
                calc.intersectionAngle,
                tc.cutLength).toString()
        def tg0 = gen.generateTextGroup("nach innen", tc.cutStart, tc.cutTop + 2, 1, -1)
        def tg1 = gen.generateTextGroup("von außen", tc.cutEnd - xOffset, tc.cutBottom - 5, 1, -1)
        def tg0r = gen.generateTextGroup("nach innen", tc.cutStart + xOffset, tc.cutTop + 2, -1, -1)
        def tg1r = gen.generateTextGroup("von außen", tc.cutEnd, tc.cutBottom - 5, -1, -1)
        def group = gen.groupFrame([p0, length, zenithLine, baseline, tg0, tg1], - (int)tc.cutStart + 10, pageHeight - 50)
        def group2 = gen.groupFrame([p0, length, tg0r, tg1r], pageWidth - xOffset  , pageHeight - 50, -1)
        def textgroup = gen.groupFrame([legend], xOffset, 0, 1, 1)
        def svg = gen.svgFrame([group, group2, textgroup])
        def dir = System.getProperty('user.home') + plotDir
        def filename = dir + '/' + String.format(filenamePattern, rLarge, rSmall, exc, angle)
        gen.writeFile(svg, filename)
    }

    static void main(String[] args) {
        calculateTubecutPlot()
//        def gen = new SvgGenerator()
//        double exc = 45
//        double angle = 30
//        double rLarge = 75.0
//        double rSmall = 25.0
//        def calc = new TubeCutCalculatorExcentric(rSmall, rLarge, angle, exc)
//        def points = calc.ellipseFromTube()
//        def svgPaths = []
//        def movedPoints = []
//        for (def offset = 90.0; offset >= 10.0; offset -= 10.0) {
//            points.each {
//                movedPoints.add(
//                        new Point(
//                                x: it.x + offset,
//                                y: it.y + calc.excentricity
//                        )
//                )
//            }
//            String path = gen.pointsToPath(movedPoints)
//            points.each {
//                movedPoints.add(
//                        new Point(
//                                x: -it.x + offset,
//                                y: -it.y + calc.excentricity
//                        )
//                )
//            }
//            path += gen.pointsToPath(movedPoints)
//            svgPaths.add(gen.pathFrame(path))
//            movedPoints.clear()
//        }
////        String path = gen.pathFrame(gen.generateSvgPath(tc))
////        String legend = gen.generateLegend(
////                calc.largeTubeRadius * 2,
////                calc.smallTubeRadius * 2,
////                calc.intersectionAngle,
////                tc.cutLength).toString()
////        String[] ingroup = [path, legend]
////        String[] group = [gen.groupFrame(ingroup, 50, 105)]
//        def circle = '<circle cx ="0" cy ="0" r ="78" style="fill:none;stroke:green;stroke-width:0.2mm;" />'
//        def xaxis = '<line x1="-50" y1="0" x2="200" y2="0" style="stroke:red;stroke-width:0.2mm;" />'
//        def yaxis = '<line x1="0" y1="-100" x2="0" y2="100" style="stroke:red;stroke-width:0.2mm;" />'
//        def eaxis = /<line x1="-50" y1="${calc.excentricity}" x2="200" y2="${calc.excentricity}" style="stroke:red;stroke-width:0.2mm;" \/>/
//        def group = gen.groupFrame(svgPaths + circle + xaxis + yaxis + eaxis)
//        def svg = gen.svgFrame([group])
//        gen.writeFile(svg, '/home/georg/tmp/tmp/ltubecut2.svg')
//        // cut graph
//        def tc = calc.calculate(0.1)
//        def length = /<line x1="${tc.cutStart}" y1="$exc" x2="${tc.cutEnd}" y2="$exc" style="stroke:red;stroke-width:0.2mm;" \/>/
//        def zenith = calc.largeTubeRadius * Math.PI / 2
//        def zenithLine = /<line x1="0" y1="${zenith}" x2="${gen.width}" y2="${zenith}" style="stroke:blue;stroke-width:0.4mm;" \/>/
//        def baseline = /<line x1="0" y1="0" x2="${gen.width}" y2="0" style="stroke:green;stroke-width:0.4mm;" \/>/
//        def pEntry0 = gen.pointsAtToPath(tc.outlines[0], tc.outlines[1])
//        def p0 = gen.pathFrame(pEntry0)
//        def legend = gen.generateLegend(
//                calc.largeTubeRadius * 2,
//                calc.smallTubeRadius * 2,
//                calc.intersectionAngle,
//                tc.cutLength).toString()
//        def tg0 = gen.generateTextGroup("nach innen", tc.cutStart, tc.cutTop + 2, 1, -1)
//        def tg1 = gen.generateTextGroup("von außen", tc.cutEnd - 20, tc.cutBottom - 5, 1, -1)
//        def tg0r = gen.generateTextGroup("nach innen", tc.cutStart + 20, tc.cutTop + 2, -1, -1)
//        def tg1r = gen.generateTextGroup("von außen", tc.cutEnd, tc.cutBottom - 5, -1, -1)
//        group = gen.groupFrame([p0, length, zenithLine, baseline, tg0, tg1], - (int)tc.cutStart, gen.height - 50)
//        def group2 = gen.groupFrame([p0, length, tg0r, tg1r], gen.width  , gen.height - 50, -1)
//        def textgroup = gen.groupFrame([legend], gen.translateX, 0, 1, 1)
//        svg = gen.svgFrame([group, group2, textgroup])
//        gen.writeFile(svg, '/home/georg/tmp/tmp/ltubecut3.svg')
    }
}

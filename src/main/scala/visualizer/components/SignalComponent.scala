package visualizer.components

import java.awt.{Color, Font}

import scalaswingcontrib.tree.Tree
import visualizer._
import visualizer.models._

import scala.swing._
import scala.swing.event._
import BorderPanel.Position.Center

class SignalComponent(dataModel: InspectionDataModel, displayModel: InspectionDisplayModel)
  extends BorderPanel {

  ///////////////////////////////////////////////////////////////////////////
  // View
  ///////////////////////////////////////////////////////////////////////////

  displayModel.tree.renderer = new SignalNameRenderer(dataModel, displayModel)
  add(displayModel.tree, Center)

  focusable = true

  ///////////////////////////////////////////////////////////////////////////
  // Controller
  ///////////////////////////////////////////////////////////////////////////

  listenTo(displayModel)
  listenTo(keys, displayModel.tree.keys)
  reactions += {
    case _: CursorSet =>
      repaint()
    case KeyReleased(_, Key.BackSpace, _, _) =>
      displayModel.removeSelectedSignals(this)
  }
}

class SignalNameRenderer(
    dataModel: InspectionDataModel, displayModel: InspectionDisplayModel)
    extends Tree.Renderer[InspectedNode] {
  var currentSignalNode = InspectedNode(-10, "asdf")
  var currentSignalIsSelected = false

  private var labelBaseLine = -1
  private var valueBaseLine = 0
  val SignalNameFont = new Font("SansSerif", Font.BOLD, 10)
  val ValueFont = new Font("SansSerif", Font.PLAIN, 8)

  override def componentFor(
      owner: Tree[_],
      value: InspectedNode,
      cellInfo: companion.CellInfo): Component = {

    currentSignalNode = value
    currentSignalIsSelected = cellInfo.isSelected
    new SignalNamePanel
  }

  class SignalNamePanel extends BorderPanel {
    peer.setOpaque(true)
    preferredSize = new Dimension(200, DrawMetrics.WaveformHeight + DrawMetrics.WaveformVerticalSpacing)

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)

      if (currentSignalNode.waveId >= 0) { // paint only signals, not groups

        if (labelBaseLine == -1) {
          // Initialized once
          val labelMetrics = g.getFontMetrics(SignalNameFont)
          val valueMetrics = g.getFontMetrics(ValueFont)
          labelBaseLine = labelMetrics.getAscent
          valueBaseLine = labelBaseLine + labelMetrics.getDescent +
            labelMetrics.getLeading + valueMetrics.getAscent
          val totalHeight = valueBaseLine + valueMetrics.getDescent
          val border = (DrawMetrics.WaveformVerticalSpacing + DrawMetrics.WaveformHeight - totalHeight) / 2
          labelBaseLine += border
          valueBaseLine += border
        }

        // Change color depending on if currentSignalIsSelected

        if (currentSignalIsSelected) {
          g.setColor(Color.blue)
        } else {
          g.setColor(Color.white)
        }
        g.fillRect(0, 0, peer.getWidth, DrawMetrics.WaveformVerticalSpacing + DrawMetrics.WaveformHeight)

        g.setFont(SignalNameFont)
        if (currentSignalIsSelected) g.setColor(Color.white) else g.setColor(Color.black)
        g.drawString(currentSignalNode.name, 1, labelBaseLine)

        g.setFont(ValueFont)
        if (currentSignalIsSelected) g.setColor(Color.white) else g.setColor(Color.blue)
        val t = dataModel.waveforms(currentSignalNode.waveId).findTransition(displayModel.cursorPosition).next()
        g.drawString(t.value.toString, 1, valueBaseLine)
      }

    }
  }
}
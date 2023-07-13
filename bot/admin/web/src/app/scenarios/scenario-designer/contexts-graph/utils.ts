type Point = { x: number; y: number };

export function svgPathRoundedCorners(points: Point[], radius: number, useFractionalRadius: boolean): string {
  if (points.length < 2) return '';
  let result = 'M ';
  points.forEach((pt) => {
    result += `${pt.x} ${pt.y} L`;
  });
  const pathString = result.substring(0, result.length - 2);

  function moveTowardsLength(movingPoint: Point, targetPoint: Point, amount: number): Point {
    const width = targetPoint.x - movingPoint.x;
    const height = targetPoint.y - movingPoint.y;

    const distance = Math.sqrt(width * width + height * height);

    return moveTowardsFractional(movingPoint, targetPoint, Math.min(1, amount / distance));
  }

  function moveTowardsFractional(movingPoint: Point, targetPoint: Point, fraction: number): Point {
    return {
      x: movingPoint.x + (targetPoint.x - movingPoint.x) * fraction,
      y: movingPoint.y + (targetPoint.y - movingPoint.y) * fraction
    };
  }

  // Adjusts the ending position of a command
  function adjustCommand(cmd: number[], newPoint: Point): void {
    if (cmd.length > 2) {
      cmd[cmd.length - 2] = newPoint.x;
      cmd[cmd.length - 1] = newPoint.y;
    }
  }

  // Gives an {x, y} object for a command's ending position
  function pointForCommand(cmd: string[]): Point {
    return {
      x: parseFloat(cmd[cmd.length - 2]),
      y: parseFloat(cmd[cmd.length - 1])
    };
  }

  // Split apart the path, handing concatonated letters and numbers
  const pathParts: string[] = pathString.split(/[,\s]/).reduce(function (parts, part) {
    const match = part.match('([a-zA-Z])(.+)');
    if (match) {
      parts.push(match[1]);
      parts.push(match[2]);
    } else {
      parts.push(part);
    }

    return parts;
  }, []);

  // Group the commands with their arguments for easier handling
  const commands = pathParts.reduce(function (cmds, part) {
    if (!isNaN(parseFloat(part)) && cmds.length) {
      cmds[cmds.length - 1].push(part);
    } else {
      cmds.push([part]);
    }

    return cmds;
  }, [] as any[]);

  // The resulting commands, also grouped
  let resultCommands = [];

  if (commands.length > 1) {
    let startPoint = pointForCommand(commands[0]);

    // Handle the close path case with a "virtual" closing line
    let virtualCloseLine: (string | number)[] | null = null;
    if (commands[commands.length - 1][0] == 'Z' && commands[0].length > 2) {
      virtualCloseLine = ['L', startPoint.x, startPoint.y];
      commands[commands.length - 1] = virtualCloseLine;
    }

    // We always use the first command (but it may be mutated)
    resultCommands.push(commands[0]);

    for (let cmdIndex = 1; cmdIndex < commands.length; cmdIndex++) {
      let prevCmd = resultCommands[resultCommands.length - 1];

      let curCmd = commands[cmdIndex];

      // Handle closing case
      let nextCmd = curCmd == virtualCloseLine ? commands[1] : commands[cmdIndex + 1];

      // Nasty logic to decide if this path is a candidite.
      if (nextCmd && prevCmd && prevCmd.length > 2 && curCmd[0] == 'L' && nextCmd.length > 2 && nextCmd[0] == 'L') {
        // Calc the points we're dealing with
        let prevPoint = pointForCommand(prevCmd);
        let curPoint = pointForCommand(curCmd);
        let nextPoint = pointForCommand(nextCmd);

        // The start and end of the cuve are just our point moved towards the previous and next points, respectivly
        let curveStart: Point, curveEnd: Point;

        if (useFractionalRadius) {
          curveStart = moveTowardsFractional(curPoint, prevCmd.origPoint || prevPoint, radius);
          curveEnd = moveTowardsFractional(curPoint, nextCmd.origPoint || nextPoint, radius);
        } else {
          curveStart = moveTowardsLength(curPoint, prevPoint, radius);
          curveEnd = moveTowardsLength(curPoint, nextPoint, radius);
        }

        // Adjust the current command and add it
        adjustCommand(curCmd, curveStart);
        curCmd.origPoint = curPoint;
        resultCommands.push(curCmd);

        // The curve control points are halfway between the start/end of the curve and
        // the original point
        let startControl = moveTowardsFractional(curveStart, curPoint, 0.5);
        let endControl = moveTowardsFractional(curPoint, curveEnd, 0.5);

        // Create the curve
        let curveCmd: any = ['C', startControl.x, startControl.y, endControl.x, endControl.y, curveEnd.x, curveEnd.y];
        // Save the original point for fractional calculations
        curveCmd.origPoint = curPoint;
        resultCommands.push(curveCmd);
      } else {
        // Pass through commands that don't qualify
        resultCommands.push(curCmd);
      }
    }

    // Fix up the starting point and restore the close path if the path was orignally closed
    if (virtualCloseLine) {
      let newStartPoint = pointForCommand(resultCommands[resultCommands.length - 1]);
      resultCommands.push(['Z']);
      adjustCommand(resultCommands[0], newStartPoint);
    }
  } else {
    resultCommands = commands;
  }

  return resultCommands.reduce(function (str, c) {
    return str + c.join(' ') + ' ';
  }, '');
}

class Filter:
    def __init__(self):
        self.coords = dict()
        self.ballIsLost = False
        self.lastBallCoords = [-1, -1, -1]
        self.lastRobotCoords = [-1, -1, -1]
        self.dribler = '--'
        self.minDist = 2000
        pass

    def change(self, name, x, y, angle):
        self.coords[name] = (x, y, angle)

    def update(self):
        if self.coords['ball'][0] != -1: # the ball is visible
            self.lastBallCoords = self.coords['ball']
            self.ballIsLost = False
            return self.coords
        else:
            if (not self.ballIsLost):
                self.ballIsLost = True
                dYellow = self.findDist(self.lastBallCoords, self.coords['yellow'])
                dBlue = self.findDist(self.lastBallCoords, self.coords['blue'])
                print 'dYellow', dYellow
                print 'dBlue', dBlue
                print self.coords
                print self.lastBallCoords
                if (min(dYellow, dBlue) < self.minDist):
                    if (dYellow < dBlue):
                        self.dribler = 'yellow'
                    else:
                        self.dribler = 'blue'
                else:
                    self.dribler = '-'
                    return self.coords
                self.lastRobotCoords = self.coords[self.dribler]
            if (self.dribler == 'yellow' or self.dribler == 'blue'):
                if (not self.coords[self.dribler][0] == -1):
                    print (self.dribler, 'is dribling!')
                    self.coords['ball'] = self.findBallCoords(self.lastBallCoords, self.lastRobotCoords, self.coords[self.dribler])
            return self.coords

    def findDist(self, p1, p2):
        return (p1[0]-p2[0])**2+(p1[1]-p2[1])**2

    def findBallCoords(self, oldBall, oldRobot, newRobot):
        x = oldBall[0] - oldRobot[0] + newRobot[0]
        y = oldBall[1] - oldRobot[1] + newRobot[1]
        return [x, y, 0]


import time
from datetime import datetime
from datetime import timedelta
from datetime import tzinfo

class TimeDateError(ValueError):

    def __init__(self, message, *args):
        self.message = message
        super(TimeDateError, self).__init__(message, *args)


ZERO = timedelta(0)

# A UTC class.
class UTC(tzinfo):
    """UTC"""

    def __str__(self):
        return "UTC"

    def utcoffset(self, dt):
        return ZERO

    def tzname(self, dt):
        return "UTC"

    def dst(self, dt):
        return ZERO

utc = UTC()

class TimeDateRange:

    beginDt = None
    endDt = None
    format = "%Y%m%d %H%M%S"

    def getBeginMs(self):
        if self.beginDt == None:
            return None
        return int(time.mktime(self.beginDt.timetuple()) * 1000)

    def getBeginStr(self):
        if self.beginDt == None:
            return None
        return time.strftime(self.format, self.beginDt.timetuple())

    def getEndMs(self):
        if self.endDt == None:
            return None
        return int(time.mktime(self.endDt.timetuple()) * 1000)

    def getEndStr(self):
        if self.endDt == None:
            return None
        return time.strftime(self.format, self.endDt.timetuple())

    def __init__(self, beginStr, endStr, hours, format="%Y%m%d-%H%M%S"):

        now = datetime.now(tz=utc)
        nowMs = TimeDateRange.unix_time_millis(now)

        if beginStr != None and endStr != None and hours != None:
            raise TimeDateError("You can not privide a begin, end, and hours")
        elif (beginStr == None and endStr == None and hours == None) or (hours == None and beginStr == None):
            raise TimeDateError("You must provide a time range")

        if hours == None:
            # calculate begin time
            beginTuple = time.strptime(beginStr, self.format)
            self.beginDt = datetime(*beginTuple[0:6], tzinfo=utc)

            # calculate end time
            # end is either specified or now
            if endStr == None or endStr == 'now':
                self.endDt = now
            else:

                self.endDt = datetime(*time.strptime(endStr, self.format)[0:6], tzinfo=utc)
                if self.endDt > now:
                    self.endDt = now
        else:
            # hours must be specified
            rangeInSec = hours * 3600
            if beginStr == None:
                # calculate begin time from end tme and hours

                # calculate end time
                # end is either specified or now
                if endStr == None or endStr == 'now':
                    self.endDt = now
                else:
                    self.endDt = datetime(*time.strptime(endStr, self.format)[0:6], tzinfo=utc)
                    if self.endDt > now:
                        self.endDt = now

                # calculate begin time
                self.beginDt = self.endDt - timedelta(milliseconds=rangeInSec * 1000)

            else:
                # calculate end time from begin tme and hours
                # calculate begin time
                self.beginDt = datetime(*time.strptime(beginStr, self.format)[0:6], tzinfo=utc)

                # calculate end time
                self.endDt = self.beginDt + timedelta(milliseconds=rangeInSec * 1000)

                if self.endDt > now:
                    self.endDt = now

        if self.beginDt > now:
            raise TimeDateError("begin date/time can not be in the future")

        if self.beginDt > self.endDt:
            raise TimeDateError("begin date/time can not be past end date/time")

    epoch = datetime.utcfromtimestamp(0).replace(tzinfo=utc)

    @classmethod
    def unix_time_millis(cls, dt):
        return (dt - TimeDateRange.epoch).total_seconds() * 1000.0



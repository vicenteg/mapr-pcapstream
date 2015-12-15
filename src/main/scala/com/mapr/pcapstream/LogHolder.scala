package com.mapr.pcapstream
/**
  * Created by vince on 12/14/15.
  */

import org.apache.log4j.Logger

object LogHolder extends Serializable {
  @transient lazy val log = Logger.getLogger(getClass.getName)
}

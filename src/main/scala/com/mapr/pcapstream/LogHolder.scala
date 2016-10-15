package com.mapr.pcapstream
/**
  * Created by vince on 12/14/15.
  */

import org.slf4j._

object LogHolder extends Serializable {
  @transient lazy val log = LoggerFactory.getLogger(getClass.getName)
}

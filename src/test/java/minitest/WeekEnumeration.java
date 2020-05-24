/**
 * Copyright 2017-2025 Evergrande Group.
 */
package minitest;

/**
 * 星期
 * 
 * @Author pengxiaofeng
 * @Since 2018年2月28日
 */
public enum WeekEnumeration {
  MONDAY("星期一", "monday",1), TUESDAY("星期二", "tuesday",2), WEDNESDAY("星期三", "wednesday",3), THURSDAY(
          "星期四", "thursday",4), FRIDAY("星期五", "friday",5), SATURDAY("星期六", "saturday",6),
  SUNDAY("星期日", "sunday",0),
  UNKNOW("未知","unknow",-1);
  //编码
  private String code;
  //描述
  private String desc;

  private int isoCode;

  /**
   * 构造器
   * @param code
   * @param desc
   */
  private WeekEnumeration(String code, String desc, int isoCode) {
    this.code = code;
    this.desc = desc;
    this.isoCode = isoCode;
  }

  public static int parserString(String dayOfWeek) {
    for (WeekEnumeration value : WeekEnumeration.values()) {
      if(value.getDesc().equals(dayOfWeek)){
        return value.getIsoCode();
      }
    }
    return -1;
  }

  public int getIsoCode() {
    return isoCode;
  }

  public static WeekEnumeration parserIsoCode(int code){
    for (WeekEnumeration value : WeekEnumeration.values()) {
      if(value.isoCode == code){
        return value;
      }
    }
    return null;
  }

  /**
 * 根据key获取描述
 * @Methods Name getDescriptionByKey
 * @Since 2018年2月28日 
 * @param key
 * @return String
 */
  public static String getDescriptionByKey(String key) {
    String result = key;
    if (key == null) {
      return result;
    }
    WeekEnumeration[] all = WeekEnumeration.values();
    for (int i = 0; i < all.length; i++) {
      if (all[i].getCode().equals(key)) {
        result = all[i].getDesc();
        break;
      }

    }
    return result;
  }
  
  /**
   * 根据desc描述 获取code
   * @Methods Name getCodeByDesc
   * @Since 2018年2月28日 
   * @param desc
   * @return String
   */
  public static WeekEnumeration getCodeByDesc(String desc) {
    WeekEnumeration[] all = WeekEnumeration.values();
    for (int i = 0; i < all.length; i++) {
      if (all[i].getDesc().equals(desc)) {
        return all[i];
      }
    }
    return null;
  }

  /**
   * 是否包含
   * @Methods Name hasKey
   * @Since 2018年2月28日 
   * @param key
   * @return boolean
   */
  public static boolean hasKey(String key) {
    boolean result = false;
    if (key == null) {
      return result;
    }
    WeekEnumeration[] all = WeekEnumeration.values();
    for (int i = 0; i < all.length; i++) {
      if (all[i].getCode().equals(key)) {
        result = true;
        break;
      }

    }
    return result;
  }

  public String getCode() {
    return code;
  }


  public String getDesc() {
    return desc;
  }

  public static int getMaxIsoCode(){
    return 6;
  }

}

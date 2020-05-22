package minitest;

import org.springframework.core.Constants;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

/**
 * 智能分析事件上报的消息类型
 */
public class AnalyEventsTypeCode {

    /**
     * 人工视频
     */
    public static final int TYPE_CODE_CAMERA_EVENT_HANDVIDEO = 20101;

    /**
     * 运动目标检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_MOVEDETECTION = 20102;

    /**
     * 遗留物品检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_LEFT_DETECTION = 20103;

    /**
     * 物品移除检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_TAKENAWAYDETECTION = 20104;

    /**
     * 拌线检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_CROSSLINE_DETECTION = 20105;

    /**
     * 入侵检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_PERIMETER_DETECTION = 20106;

    /**
     * 逆行检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_RETROGRADEDETECTION = 20107;

    /**
     * 徘徊检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_WANDERDETECTION = 20108;

    /**
     * 流量统计
     */
    public static final int TYPE_CODE_CAMERA_EVENT_FLUX_STAT = 20109;

    /**
     * 密度检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_DENSITYDETECTION = 20110;

    /**
     * 视频异常
     */
    public static final int TYPE_CODE_CAMERA_EVENT_VIDEOEXCEPTION = 20111;

    /**
     * 快速移动
     */
    public static final int TYPE_CODE_CAMERA_EVENT_FASTMOVEDETECTION = 20112;

    /**
     * 人脸检测
     */
    public static final int TYPE_CODE_CAMERA_EVENT_FACEDTECT = 20113;

    /**
     * 人脸识别
     */
    public static final int TYPE_CODE_CAMERA_EVENT_FACERECOGINITION = 20114;

    /**
     * 车位有车
     */
    public static final int TYPE_CODE_CAMERA_EVENT_PARKINGSPACE	 = 20117;

    /**
     * 车位无车
     */
    public static final int TYPE_CODE_CAMERA_EVENT_NOPARKINGSPACE  = 20118;

    /**
     * 违停
     */
    public static final int TYPE_CODE_CAMERA_EVENT_ILLEGALPARKINGSPACE = 20119;

    /**
     * 交通拥堵
     */
    public static final int TYPE_CODE_CAMERA_EVENT_TRAFFIC_CONGESTION = 20120;

    /**
     * 穿越围栏
     */
    public static final int TYPE_CODE_CAMERA_EVENT_CROSS_FENCE = 20121;

    /**
     * 行人聚集
     */
    public static final int TYPE_CODE_CAMERA_EVENT_PEOPLE_GATHER = 20122;

    /**
     * 行人事件
     */
    public static final int TYPE_CODE_CAMERA_EVENT_PEDESTRIAN = 20123;

    /**
     * 存储设备磁盘故障告警
     */
    public static final int TYPE_CODE_CAMERA_FAILURE_ALARM_DISK = 20200;

    /**
     * 存储设备风扇故障告警
     */
    public static final int TYPE_CODE_CAMERA_FAILURE_ALARM_FAN = 20201;

    /**
     * 视频遮挡故障告警
     */
    public static final int TYPE_CODE_CAMERA_FAILURE_ALARM_OCCLUSION = 20202;

    /**
     * 场景变更故障告警
     */
    public static final int TYPE_CODE_CAMERA_FAILURE_SCENE_CHANGE = 20203;

    private AnalyEventsTypeCode() {
        throw new IllegalStateException("Constants class");
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Constants constants = new Constants(AnalyEventsTypeCode.class);
        Field fieldCache = Constants.class.getDeclaredField("fieldCache");fieldCache.setAccessible(true);
        Map<String, Object> map = (Map<String, Object>)fieldCache.get(constants);
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> next = iterator.next();
            String key = next.getKey();
            Object value = next.getValue();
            System.out.println(key+"|"+value);
        }
    }
}

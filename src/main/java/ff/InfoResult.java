package ff;


        import com.google.gson.annotations.SerializedName;
        import lombok.Data;
        import lombok.ToString;

        import java.util.List;

/**
 * Created by wangjinliang on 2018/2/5.
 */
@Data
@ToString
public class InfoResult {
    @SerializedName("app_msg_list")
    private List<Info> appMsgList;
    @SerializedName("app_msg_cnt")
    private Integer totalCount;
}

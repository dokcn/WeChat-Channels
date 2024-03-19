package fun.dokcn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TriggerInfo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private LocalTime time;

    private boolean repeated;

    @TableField(exist = false)
    private LocalDateTime nextTriggerTime;

}

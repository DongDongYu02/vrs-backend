package cn.dong.nexus.modules.vrs.mapper;

import cn.dong.nexus.modules.vrs.domain.bo.EkpReviewBO;
import cn.dong.nexus.modules.vrs.domain.bo.EkpUserBO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface EkpCommonMapper {


    @Select("""
                    SELECT fd_id AS id,fd_login_name AS login_name,fd_mobile_no AS phone
                    FROM sys_org_person
                    WHERE fd_mobile_no = #{phone}
                    AND fd_can_login = 1
                    AND fd_leave_date IS NULL
            """)
    EkpUserBO selectEkpUserByPhone(@Param("phone") String phone);

    @Select("""
            SELECT fd_id AS id,
                   fd_name AS name
            FROM sys_org_element
            WHERE fd_id = #{id}
            """)
    EkpUserBO selectEkpUserById(@Param("id") String id);

    @Delete("""
            delete FROM km_review_main WHERE fd_id = #{reviewId}
            """)
    void deleteBookingReview(@Param("reviewId") String reviewId);

    @Delete("""
            delete FROM km_review_main_areader WHERE fd_doc_id = #{reviewId}
            """)
    void deleteReviewAreader(@Param("reviewId") String reviewId);

    @Delete("""
            delete FROM km_review_main_oreader WHERE fd_doc_id = #{reviewId}
            """)
    void deleteReviewOreader(@Param("reviewId") String reviewId);

    @Delete("""
            delete FROM sys_notify_todo WHERE fd_model_id = #{reviewId}
            """)
    void deleteReviewTodo(@Param("reviewId") String reviewId);

    @Select("""
             SELECT extend_data_xml
                    FROM km_review_main
                    WHERE fd_id = #{ekpReviewId}
            """)
    EkpReviewBO selectReviewById(@Param("ekpReviewId") String ekpReviewId);

    @Update("""
                UPDATE km_review_main
                    SET extend_data_xml = #{content}
                    WHERE fd_id = #{ekpReviewId}
            """)
    void updateReviewContent(@Param("ekpReviewId") String ekpReviewId, @Param("content") String content);
}

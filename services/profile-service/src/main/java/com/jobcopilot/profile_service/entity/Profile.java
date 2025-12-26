package com.jobcopilot.profile_service.entity;

import com.jobcopilot.profile_service.entity.values.Derived;
import com.jobcopilot.profile_service.entity.values.Resume;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder(toBuilder = true)
@Document(collection = "profiles")
@CompoundIndex(
    name = "userId_displayName_idx",
    def = "{'userId': 1, 'displayName': 1}",
    unique = true)
public class Profile extends BaseEntity {

  @Id private String id;

  @Indexed(unique = false)
  private String userId;

  private String displayName;

  private ProfileStatus status;

  private Resume resume;

  private Derived derived;
}

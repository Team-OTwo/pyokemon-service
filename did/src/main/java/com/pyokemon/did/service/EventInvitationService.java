package com.pyokemon.did.service;

import com.pyokemon.did.remote.commonAcaPy.dto.request.WalletRequest.AcaPyCreateWalletRequest;
import com.pyokemon.did.remote.commonAcaPy.dto.response.WalletResponse;

public interface EventInvitationService {

  /**
   * 미디에이터 초대장을 생성합니다.
   *
   * @return 미디에이터 초대장 응답
   */
  WalletResponse.AcaPyCreateWalletResponse getMediatorInvitation();

  void createEventInvitation(AcaPyCreateWalletRequest request);

}

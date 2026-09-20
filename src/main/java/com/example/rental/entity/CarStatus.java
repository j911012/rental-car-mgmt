package com.example.rental.entity;

import java.util.List;

public enum CarStatus {

	AVAILABLE,
	RENTED,
	MAINTENANCE;

	/**
	 * 車両登録・車両編集の画面で、手動で選択できるステータス。
	 * RENTEDは貸出登録・返却処理でのみ設定される状態のため、手動では選べないようにする。
	 */
	public static List<CarStatus> selectableValues() {
		return List.of(AVAILABLE, MAINTENANCE);
	}

}

package com.example.rental.exception;

import lombok.Getter;

/**
 * 業務ルール違反を表す例外。
 * Service層で送出し、Controller層で捕まえて画面にエラーを表示する。
 *
 * <p>
 * {@code field} に項目名を指定すると、その入力欄のエラーとして表示する。
 * 特定の項目に紐づかないエラーは {@code field} を指定せず、画面全体のエラーとして表示する。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** エラー対象の項目名。特定の項目に紐づかない場合はnull */
	private final String field;

	/** messages.propertiesのメッセージキー */
	private final String messageKey;

	public BusinessException(String field, String messageKey) {
		super(messageKey);
		this.field = field;
		this.messageKey = messageKey;
	}

	public BusinessException(String messageKey) {
		this(null, messageKey);
	}

}

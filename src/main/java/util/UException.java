/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package util;

    import java.util.Locale;


    /**
 * Library exception carrying optional numeric error information and localized message lookup.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class UException extends RuntimeException   {
    	private static final long serialVersionUID = 1L;

	
		
        public static int UERootDeletionError                    = 10000;
        public static int UERootParentExistsDeletionError = 10001;
        public static int UEDirIsSystemUpdateError          = 10002;
        public static int UEUserAuthorizationError            = 10010;
        public static int UEWrongServiceIDError               = 10020;
        
        
        public UException( String aErrorMessage ) {
            super(aErrorMessage);
        }

        public UException( int aErrorCode, Locale l ) {
            super(getErrorMessage(aErrorCode, l));
        }
        
        
        
        public static String getErrorMessage( int aErrorCode, Locale l ) {
            String res = null;
            if ( l.equals(Util.RUSSIAN_LOCALE) ) {
                if (aErrorCode == UERootDeletionError) res = "Удаление корневого каталога запрещено!";
                if (aErrorCode == UERootParentExistsDeletionError) res = "Удаление запрещено! На данную запись существуют ссылки!";
                if (aErrorCode == UEDirIsSystemUpdateError) res = "Редактирование/удаление данного каталога запрещены (системный каталог) !";
                if (aErrorCode == UEUserAuthorizationError) res = "Ошибка авторизации пользователя! Неверное имя или пароль!";
                if (aErrorCode == UEWrongServiceIDError) res = "Страница не найдена (страница не существует или неверный ID сервиса)!";

            }
            else
            if ( l.equals(Util.ENGLISH_LOCALE) ) {
                if (aErrorCode == UERootDeletionError) res = "Root deletion is forbidden!";
                if (aErrorCode == UERootParentExistsDeletionError) res = "Level has child nodes! Deletion is forbidden!";
                if (aErrorCode == UEDirIsSystemUpdateError) res = "Root edit/deletion is forbidden (this is embeded system folder)!";
                if (aErrorCode == UEUserAuthorizationError) res = "User authorization error! Wrong user name or password!";
                if (aErrorCode == UEWrongServiceIDError) res = "Page not found error (page not exists or wrong service ID)";
            }
            return res;
        }

        public UException(Throwable cause)
        {
            super(cause);
        }

        public UException(String message, Throwable cause)
        {
            super(message, cause);
        }


}

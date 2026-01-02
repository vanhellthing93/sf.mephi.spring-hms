package sf.mephi.common.constants;

/**
 * Роли пользователей в системе
 */
public enum Role {
    /**
     * Обычный пользователь - может создавать бронирования
     */
    USER,

    /**
     * Администратор - полный доступ к управлению отелями, номерами, пользователями
     */
    ADMIN;

    /**
     * Получить роль с префиксом ROLE_ для Spring Security
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Проверка, является ли роль административной
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
}

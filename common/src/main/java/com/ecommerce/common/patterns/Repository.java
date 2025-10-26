package com.ecommerce.common.patterns;

import java.util.List;
import java.util.Optional;

/**
 * 倉儲模式基礎介面，遵循 ISP 和 DIP 原則
 * 定義資料存取的抽象契約
 */
public interface Repository<T, ID> {
    
    /**
     * 儲存實體
     * @param entity 要儲存的實體
     * @return 儲存後的實體
     */
    T save(T entity);
    
    /**
     * 根據 ID 查找實體
     * @param id 實體 ID
     * @return 找到的實體，如果不存在則為空
     */
    Optional<T> findById(ID id);
    
    /**
     * 查找所有實體
     * @return 所有實體列表
     */
    List<T> findAll();
    
    /**
     * 根據 ID 刪除實體
     * @param id 要刪除的實體 ID
     */
    void deleteById(ID id);
    
    /**
     * 檢查實體是否存在
     * @param id 實體 ID
     * @return 是否存在
     */
    boolean existsById(ID id);
}
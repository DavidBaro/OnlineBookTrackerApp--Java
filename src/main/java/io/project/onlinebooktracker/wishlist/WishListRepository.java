package io.project.onlinebooktracker.wishlist;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface WishListRepository extends CassandraRepository<WishListBook, String> {

    // Finding record in the table with the user id and bookId
    WishListBook findByIdAndBookId(String id, String bookId);

    // Getting all the books in the table by the user id
    // Also Applying Pagination
    Slice<WishListBook> findAllById(String id, Pageable pageable);
}

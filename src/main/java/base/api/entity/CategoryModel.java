package base.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "categories")
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CategoryModel extends BaseModel {

    @ToString.Include
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference(value = "cat-parent")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private CategoryModel parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "cat-parent")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<CategoryModel> children = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-categories")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private UserModel userModel;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "cat-pc")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<ProductCategoryModel> productCategories = new ArrayList<>();
}

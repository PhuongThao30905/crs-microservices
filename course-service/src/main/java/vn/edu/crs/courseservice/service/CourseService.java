package vn.edu.crs.courseservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.courseservice.dto.CourseDTO;
import vn.edu.crs.courseservice.entity.Course;
import vn.edu.crs.courseservice.repository.CourseRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // Buổi 3 - Tìm kiếm + phân trang
    public Page<CourseDTO> search(String keyword, Pageable pageable) {

        Page<Course> page = (keyword == null || keyword.isBlank())
                ? courseRepository.findAll(pageable)
                : courseRepository.findByTenMonHocContainingIgnoreCase(
                keyword,
                pageable
        );

        return page.map(this::toDTO);
    }


    // Buổi 3 - Trừ 1 chỗ khi sinh viên đăng ký
    @Transactional
    public CourseDTO reserveSeat(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay mon hoc id = " + courseId
                        )
                );

        // Nếu đã hết chỗ thì không cho đăng ký
        if (course.getSoChoConLai() <= 0) {
            throw new IllegalStateException(
                    "Mon hoc da het cho, khong the dang ky"
            );
        }

        // Trừ 1 chỗ
        course.setSoChoConLai(
                course.getSoChoConLai() - 1
        );

        return toDTO(
                courseRepository.save(course)
        );
    }


    // Buổi 3 - Hoàn lại 1 chỗ khi sinh viên hủy đăng ký
    @Transactional
    public CourseDTO releaseSeat(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay mon hoc id = " + courseId
                        )
                );

        // Chỉ tăng nếu số chỗ còn lại chưa bằng số chỗ tối đa
        if (course.getSoChoConLai() < course.getSoChoToiDa()) {

            course.setSoChoConLai(
                    course.getSoChoConLai() + 1
            );
        }

        return toDTO(
                courseRepository.save(course)
        );
    }


    // Lấy tất cả môn học - giữ lại từ Buổi 2
    public List<CourseDTO> getAll() {

        return courseRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    // Lấy môn học theo id
    public CourseDTO getById(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay mon hoc id = " + id
                        )
                );

        return toDTO(course);
    }


    // Tạo môn học mới
    public CourseDTO create(CourseDTO dto) {

        if (courseRepository.existsByTenMonHocIgnoreCase(
                dto.getTenMonHoc()
        )) {

            throw new IllegalArgumentException(
                    "Ten mon hoc da ton tai"
            );
        }
        Course course = new Course();
        course.setTenMonHoc(dto.getTenMonHoc());
        course.setSoTinChi(dto.getSoTinChi());
        course.setSoChoToiDa(dto.getSoChoToiDa());
        course.setSoChoConLai(
                dto.getSoChoToiDa()
        );

        Course savedCourse =
                courseRepository.save(course);

        return toDTO(savedCourse);
    }
    public CourseDTO update(Long id, CourseDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay mon hoc id = " + id
                        )
                );
        course.setTenMonHoc(dto.getTenMonHoc());
        course.setSoTinChi(dto.getSoTinChi());
        course.setSoChoToiDa(dto.getSoChoToiDa());
        Course updatedCourse =
                courseRepository.save(course);

        return toDTO(updatedCourse);
    }
    public void delete(Long id) {

        if (!courseRepository.existsById(id)) {

            throw new NoSuchElementException(
                    "Khong tim thay mon hoc id = " + id
            );
        }
        courseRepository.deleteById(id);
    }
    private CourseDTO toDTO(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getTenMonHoc(),
                course.getSoTinChi(),
                course.getSoChoToiDa(),
                course.getSoChoConLai()
        );
    }
}
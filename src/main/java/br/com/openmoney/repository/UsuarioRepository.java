package br.com.openmoney.repository;

import br.com.openmoney.domain.Usuario;
import br.com.openmoney.infrastructure.db.DatabaseConfig;
import br.com.openmoney.infrastructure.exception.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UsuarioRepository implements CrudRepository<Usuario, Long> {

  

    @Override
    public Usuario salvar(Usuario u) {
        final String sql =
                "INSERT INTO usuario (nome, email, senha) " +
                "VALUES (?, ?, ?) RETURNING id, data_cadastro";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenha());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u.setId(rs.getLong("id"));
                    u.setDataCadastro(rs.getDate("data_cadastro").toLocalDate());
                }
            }
            return u;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao salvar usuario: " + e.getMessage(), e);
        }
    }

    

    @Override
    public Usuario atualizar(Usuario u) {
        final String sql =
                "UPDATE usuario SET nome = ?, email = ?, senha = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenha());
            ps.setLong(4, u.getId());
            ps.executeUpdate();
            return u;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar usuario: " + e.getMessage(), e);
        }
    }

    
    @Override
    public void deletar(Long id) {
        final String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar usuario: " + e.getMessage(), e);
        }
    }

   

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        final String sql =
                "SELECT id, nome, email, senha, data_cadastro FROM usuario WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar usuario por id: " + e.getMessage(), e);
        }
    }

  

    @Override
    public List<Usuario> listarTodos() {
        final String sql =
                "SELECT id, nome, email, senha, data_cadastro FROM usuario ORDER BY nome";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Usuario> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar usuarios: " + e.getMessage(), e);
        }
    }

  
    public Optional<Usuario> buscarPorEmail(String email) {
        final String sql =
                "SELECT id, nome, email, senha, data_cadastro FROM usuario WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar usuario por email: " + e.getMessage(), e);
        }
    }

  
    public boolean existeEmail(String email) {
        final String sql = "SELECT 1 FROM usuario WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao verificar email: " + e.getMessage(), e);
        }
    }

   
    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("senha"),
                rs.getDate("data_cadastro").toLocalDate()
        );
    }
}

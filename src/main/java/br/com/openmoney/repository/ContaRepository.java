package br.com.openmoney.repository;

import br.com.openmoney.domain.Conta;
import br.com.openmoney.domain.TipoConta;
import br.com.openmoney.infrastructure.db.DatabaseConfig;
import br.com.openmoney.infrastructure.exception.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ContaRepository implements CrudRepository<Conta, Long> {



    @Override
    public Conta salvar(Conta c) {
        final String sql =
                "INSERT INTO conta (nome, tipo, cor, saldo, id_usuario) " +
                "VALUES (?, ?::tipo_conta, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getTipo().name());
            ps.setString(3, c.getCor());
            ps.setBigDecimal(4, c.getSaldo());
            ps.setLong(5, c.getUsuarioId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) c.setId(rs.getLong("id"));
            }
            return c;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao salvar conta: " + e.getMessage(), e);
        }
    }

   

    @Override
    public Conta atualizar(Conta c) {
        final String sql =
                "UPDATE conta SET nome = ?, tipo = ?::tipo_conta, cor = ?, saldo = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getTipo().name());
            ps.setString(3, c.getCor());
            ps.setBigDecimal(4, c.getSaldo());
            ps.setLong(5, c.getId());
            ps.executeUpdate();
            return c;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar conta: " + e.getMessage(), e);
        }
    }



    @Override
    public void deletar(Long id) {
        final String sql = "DELETE FROM conta WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar conta: " + e.getMessage(), e);
        }
    }



    @Override
    public Optional<Conta> buscarPorId(Long id) {
        final String sql =
                "SELECT id, nome, tipo, cor, saldo, id_usuario FROM conta WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar conta por id: " + e.getMessage(), e);
        }
    }



    public List<Conta> listarPorUsuario(Long usuarioId) {
        final String sql =
                "SELECT id, nome, tipo, cor, saldo, id_usuario " +
                "FROM conta WHERE id_usuario = ? ORDER BY nome";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Conta> lista = new ArrayList<>();
                while (rs.next()) lista.add(mapear(rs));
                return lista;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar contas por usuario: " + e.getMessage(), e);
        }
    }

   

    @Override
    public List<Conta> listarTodos() {
        final String sql =
                "SELECT id, nome, tipo, cor, saldo, id_usuario FROM conta ORDER BY id_usuario, nome";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Conta> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar contas: " + e.getMessage(), e);
        }
    }



    private Conta mapear(ResultSet rs) throws SQLException {
        return new Conta(
                rs.getLong("id"),
                rs.getString("nome"),
                TipoConta.valueOf(rs.getString("tipo")),
                rs.getString("cor"),
                rs.getBigDecimal("saldo"),
                rs.getLong("id_usuario")
        );
    }
}
